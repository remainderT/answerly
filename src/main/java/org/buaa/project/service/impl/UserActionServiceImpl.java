package org.buaa.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.MessageTypeEnum;
import org.buaa.project.common.enums.UserActionTypeEnum;
import org.buaa.project.dao.entity.CommentDO;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.entity.UserActionDO;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.CommentMapper;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dao.mapper.UserActionMapper;
import org.buaa.project.dao.mapper.UserMapper;
import org.buaa.project.mq.MqEvent;
import org.buaa.project.mq.MqProducer;
import org.buaa.project.service.UserActionService;
import org.buaa.project.toolkit.RedisCount;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;

import static org.buaa.project.common.consts.RedisCacheConstants.ACTIVITY_SCORE_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.COMMENT_COUNT_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.COMMENT_LIKE_SET_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_COLLECT_SET_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_COUNT_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_LIKE_SET_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.USER_ACTION_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.USER_COUNT_KEY;
import static org.buaa.project.common.consts.SystemConstants.COMMENT_SCORE;
import static org.buaa.project.common.consts.SystemConstants.LIKE_SCORE;
import static org.buaa.project.common.consts.SystemConstants.USEFUL_SCORE;
import static org.buaa.project.common.consts.SystemConstants.VIEW_SCORE;

/**
 * 用户行为接口实现
 */
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl extends ServiceImpl<UserActionMapper, UserActionDO> implements UserActionService {

    private final MqProducer producer;

    private final RedisCount redisCount;

    private final RedissonClient redissonClient;

    private final UserMapper userMapper;

    private final QuestionMapper questionMapper;

    private final CommentMapper commentMapper;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserActionDO getUserAction(Long userId, EntityTypeEnum entityType, Long entityId) {
        LambdaQueryWrapper<UserActionDO> queryWrapper = Wrappers.lambdaQuery(UserActionDO.class)
                .eq(UserActionDO::getUserId, userId)
                .eq(UserActionDO::getEntityId, entityId)
                .eq(UserActionDO::getEntityType, entityType);

        UserActionDO userActionDO =  baseMapper.selectOne(queryWrapper);
        if (userActionDO == null) {
            userActionDO = UserActionDO.builder()
                    .userId(userId)
                    .entityType(entityType.type())
                    .entityId(entityId)
                    .likeStat(0)
                    .collectStat(0)
                    .collectStat(0)
                    .build();
            baseMapper.insert(userActionDO);
            redisCount.zIncr(ACTIVITY_SCORE_KEY , userId.toString(), VIEW_SCORE);
        }
        return userActionDO;
    }

    @Override
    public void updateLastViewTime(Long userId, EntityTypeEnum entityType, Long entityId) {
        LambdaQueryWrapper<UserActionDO> queryWrapper = Wrappers.lambdaQuery(UserActionDO.class)
                .eq(UserActionDO::getUserId, userId)
                .eq(UserActionDO::getEntityId, entityId)
                .eq(UserActionDO::getEntityType, entityType);

        UserActionDO userAction = baseMapper.selectOne(queryWrapper);
        if (userAction != null) {
            userAction.setLastViewTime(new Date());
            baseMapper.updateById(userAction);
        } else {
            UserActionDO newUserAction = UserActionDO.builder()
                    .userId(userId)
                    .entityType(entityType.type())
                    .entityId(entityId)
                    .lastViewTime(new Date())
                    .build();
            baseMapper.insert(newUserAction);
        }
    }

    @Override
    @Transactional
    public void collectAndLikeAndUseful(Long userId, EntityTypeEnum entityType, Long entityId, Long entityUserId, UserActionTypeEnum actionType) {
        HashMap<String, Object> data = new HashMap<>();
        MessageTypeEnum messageType = null;
        boolean isPositive = false;
        RLock lock = redissonClient.getLock(USER_ACTION_KEY + userId + "_" + entityId);
        lock.lock();
        try {
            UserActionDO userAction = getUserAction(userId, entityType, entityId);
            UserDO userFrom;
            UserDO userTo;
            CommentDO comment;
            QuestionDO question;
            switch (actionType) {
                case LIKE:
                    messageType = MessageTypeEnum.LIKE;
                    isPositive = userAction.getLikeStat() == 0;
                    userAction.setLikeStat(isPositive ? 1 : 0);
                    userTo = userMapper.selectById(entityUserId);
                    userTo.setLikeCount(userTo.getLikeCount() + (isPositive ? 1 : -1));
                    userMapper.updateById(userTo);
                    redisCount.hIncr(USER_COUNT_KEY + entityUserId, "like", isPositive ? 1 : -1);
                    if (entityType == EntityTypeEnum.QUESTION) {
                        question = questionMapper.selectById(entityId);
                        question.setLikeCount(question.getLikeCount() + (isPositive ? 1 : -1));
                        questionMapper.updateById(question);
                        redisCount.hIncr(QUESTION_COUNT_KEY + entityId, "like", isPositive ? 1 : -1);
                        if (isPositive) {
                            stringRedisTemplate.opsForSet().add(QUESTION_LIKE_SET_KEY + entityId, userId.toString());
                        } else {
                            stringRedisTemplate.opsForSet().remove(QUESTION_LIKE_SET_KEY + entityId, userId.toString());
                        }
                    } else {
                        comment = commentMapper.selectById(entityId);
                        comment.setLikeCount(comment.getLikeCount() + (isPositive ? 1 : -1));
                        commentMapper.updateById(comment);
                        redisCount.hIncr(COMMENT_COUNT_KEY + entityId, "like", isPositive ? 1 : -1);
                        if (isPositive) {
                            stringRedisTemplate.opsForSet().add(COMMENT_LIKE_SET_KEY + entityId, userId.toString());
                        } else {
                            stringRedisTemplate.opsForSet().remove(COMMENT_LIKE_SET_KEY + entityId, userId.toString());
                        }
                    }
                    redisCount.zIncr(ACTIVITY_SCORE_KEY , userId.toString(), isPositive ? LIKE_SCORE: -LIKE_SCORE);
                    break;

                case COLLECT:
                    messageType = MessageTypeEnum.COLLECT;
                    isPositive = userAction.getCollectStat() == 0;
                    userAction.setCollectStat(isPositive ? 1 : 0);
                    userFrom = userMapper.selectById(userId);
                    userFrom.setCollectCount(userFrom.getCollectCount() + (isPositive ? 1 : -1));
                    userMapper.updateById(userFrom);
                    redisCount.hIncr(USER_COUNT_KEY + userId, "collect", isPositive ? 1 : -1);
                    redisCount.hIncr(QUESTION_COUNT_KEY + entityId, "collect", isPositive ? 1 : -1);
                    if (isPositive) {
                        stringRedisTemplate.opsForSet().add(QUESTION_COLLECT_SET_KEY + entityId, userId.toString());
                    } else {
                        stringRedisTemplate.opsForSet().remove(QUESTION_COLLECT_SET_KEY + entityId, userId.toString());
                    }
                    redisCount.zIncr(ACTIVITY_SCORE_KEY , userId.toString(), isPositive ? COMMENT_SCORE: -COMMENT_SCORE);
                    break;

                case USEFUL:
                    messageType = MessageTypeEnum.USEFUL;
                    isPositive = userAction.getUsefulStat() == 0;
                    userAction.setUsefulStat(isPositive ? 1 : 0);
                    comment = commentMapper.selectById(entityId);
                    comment.setUseful(isPositive ? 1 : 0);
                    userTo = userMapper.selectById(entityUserId);
                    userTo.setUsefulCount(userTo.getUsefulCount() + (isPositive ? 1 : -1));
                    userMapper.updateById(userTo);
                    commentMapper.updateById(comment);
                    redisCount.hIncr(USER_COUNT_KEY + entityUserId, "useful", isPositive ? 1 : -1);
                    redisCount.zIncr(ACTIVITY_SCORE_KEY , userId.toString(), isPositive ? USEFUL_SCORE: -USEFUL_SCORE);
                    break;

                default:
                    break;
            }
            baseMapper.updateById(userAction);
            data.put("isPositive", isPositive);

            MqEvent event = MqEvent.builder()
                    .messageType(messageType)
                    .entityType(entityType)
                    .userId(userId)
                    .entityId(entityId)
                    .entityUserId(entityUserId)
                    .generateId(userAction.getId())
                    .data(data)
                    .build();
            producer.messageSend(event);
        } finally {
            lock.unlock();
        }

    }

}
