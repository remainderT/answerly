package org.buaa.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.MessageTypeEnum;
import org.buaa.project.common.enums.UserActionTypeEnum;
import org.buaa.project.dao.entity.CommentDO;
import org.buaa.project.dao.entity.UserActionDO;
import org.buaa.project.dao.mapper.CommentMapper;
import org.buaa.project.dao.mapper.UserActionMapper;
import org.buaa.project.mq.MqEvent;
import org.buaa.project.mq.MqProducer;
import org.buaa.project.service.UserActionService;
import org.buaa.project.toolkit.RedisCount;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

import static org.buaa.project.common.consts.RedisCacheConstants.COMMENT_COUNT_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_COUNT_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.USER_COUNT_KEY;

/**
 * 用户行为接口实现
 */
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl extends ServiceImpl<UserActionMapper, UserActionDO> implements UserActionService {

    private final MqProducer producer;

    private final RedisCount redisCount;

    private final CommentMapper commentMapper;

    @Override
    public UserActionDO getUserAction(Long userId, EntityTypeEnum entityType, Long entityId) {
        LambdaQueryWrapper<UserActionDO> queryWrapper = Wrappers.lambdaQuery(UserActionDO.class)
                .eq(UserActionDO::getUserId, userId)
                .eq(UserActionDO::getEntityId, entityId)
                .eq(UserActionDO::getEntityType, entityType);

        return baseMapper.selectOne(queryWrapper);
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
    public void userAction(Long userId, EntityTypeEnum entityType, Long entityId, Long entityUserId, UserActionTypeEnum actionType) {
        UserActionDO userAction = getUserAction(userId, entityType, entityId);
        HashMap<String, Object> data = new HashMap<>();
        MessageTypeEnum messageType = null;
        boolean isPositive = false;
        switch (actionType) {
            case LIKE:
                messageType = MessageTypeEnum.LIKE;
                isPositive = userAction.getLikeStat() == 0;
                userAction.setLikeStat(isPositive ? 1 : 0);
                redisCount.hIncr(USER_COUNT_KEY + entityUserId, "like", isPositive ? 1 : -1);
                if (entityType == EntityTypeEnum.QUESTION) {
                    redisCount.hIncr(QUESTION_COUNT_KEY + entityId, "like", isPositive ? 1 : -1);
                } else {
                    redisCount.hIncr(COMMENT_COUNT_KEY + entityId, "like", isPositive ? 1 : -1);
                }
                break;

            case COLLECT:
                messageType = MessageTypeEnum.COLLECT;
                isPositive = userAction.getCollectStat() == 0;
                userAction.setCollectStat(isPositive ? 1 : 0);
                redisCount.hIncr(USER_COUNT_KEY + userId, "collect", isPositive ? 1 : -1);
                break;

            case COMMENT:
                messageType = MessageTypeEnum.COMMENT;
                isPositive = userAction.getCommentStat() == 0;
                userAction.setCommentStat(isPositive ? 1 : 0);
                if (entityType == EntityTypeEnum.QUESTION) {
                    redisCount.hIncr(QUESTION_COUNT_KEY + entityId, "comment", isPositive ? 1 : -1);
                } else {
                    CommentDO comment = commentMapper.selectById(entityId);
                    redisCount.hIncr(COMMENT_COUNT_KEY + comment.getQuestionId(), "comment", isPositive ? 1 : -1);
                }
                break;

            default:
                break;
        }
        baseMapper.updateById(userAction);
        data.put("isPositive", isPositive);
        data.put("userActionId", userAction.getId());

        MqEvent event = MqEvent.builder()
                .messageType(messageType)
                .entityType(entityType)
                .userId(userId)
                .entityId(entityId)
                .entityUserId(entityUserId)
                .data(data)
                .build();
        producer.send(event);
    }

}
