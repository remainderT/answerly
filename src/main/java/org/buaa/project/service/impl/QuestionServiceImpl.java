package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.convention.exception.ClientException;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.MessageTypeEnum;
import org.buaa.project.common.enums.UserActionTypeEnum;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dao.mapper.UserActionMapper;
import org.buaa.project.dto.req.question.QuestionCollectPageReqDTO;
import org.buaa.project.dto.req.question.QuestionCollectReqDTO;
import org.buaa.project.dto.req.question.QuestionLikeReqDTO;
import org.buaa.project.dto.req.question.QuestionMinePageReqDTO;
import org.buaa.project.dto.req.question.QuestionRecentPageReqDTO;
import org.buaa.project.dto.req.question.QuestionSolveReqDTO;
import org.buaa.project.dto.req.question.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.resp.QuestionPageRespDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.mq.MqEvent;
import org.buaa.project.mq.MqProducer;
import org.buaa.project.service.QuestionService;
import org.buaa.project.service.UserActionService;
import org.buaa.project.toolkit.RedisCount;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.buaa.project.common.consts.RedisCacheConstants.ACTIVITY_SCORE_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.HOT_QUESTION_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_CONTENT_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_COUNT_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_LOCK_KEY;
import static org.buaa.project.common.consts.SystemConstants.QUESTION_SCORE;
import static org.buaa.project.common.enums.QAErrorCodeEnum.QUESTION_ACCESS_CONTROL_ERROR;
import static org.buaa.project.common.enums.QAErrorCodeEnum.QUESTION_NULL;

/**
 * The type Question service.
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, QuestionDO> implements QuestionService {

    private final UserActionMapper userActionMapper;
    private final UserActionService userActionService;
    private final RedisCount redisCount;
    private final StringRedisTemplate stringRedisTemplate;
    private final MqProducer producer;
    private final RedissonClient redissonClient;

    @Override
    public void uploadQuestion(QuestionUploadReqDTO requestParam) {
        QuestionDO question = BeanUtil.toBean(requestParam, QuestionDO.class);
        question.setUserId(UserContext.getUserId());
        question.setUsername(UserContext.getUsername());
        baseMapper.insert(question);
        redisCount.zIncr(ACTIVITY_SCORE_KEY, UserContext.getUserId().toString(), QUESTION_SCORE);

        dataSync(question, MessageTypeEnum.INSERT);
    }

    @Override
    public void updateQuestion(QuestionUpdateReqDTO requestParam){
        Long id = requestParam.getId();
        checkQuestionExist(id);
        checkQuestionOwner(id);

        LambdaUpdateWrapper<QuestionDO> queryWrapper = Wrappers.lambdaUpdate(QuestionDO.class)
                .eq(QuestionDO::getId, requestParam.getId());
        QuestionDO questionDO = baseMapper.selectOne(queryWrapper);
        BeanUtils.copyProperties(requestParam, questionDO);
        baseMapper.update(questionDO, queryWrapper);
        String question = JSON.toJSONString(questionDO);
        stringRedisTemplate.opsForValue().set(QUESTION_CONTENT_KEY + id, question, 1, TimeUnit.HOURS);

        dataSync(questionDO, MessageTypeEnum.UPDATE);
    }

    @Override
    public void deleteQuestion(Long id) {
        checkQuestionExist(id);
        if(!UserContext.getUserType().equals("admin")){
            checkQuestionOwner(id);
        }

        QuestionDO question = baseMapper.selectById(id);
        question.setDelFlag(1);
        baseMapper.updateById(question);

        stringRedisTemplate.delete(QUESTION_CONTENT_KEY + id);
        dataSync(question, MessageTypeEnum.DELETE);
    }

    @Override
    public void likeQuestion(QuestionLikeReqDTO requestParam) {
        checkQuestionExist(requestParam.getId());

        userActionService.collectAndLikeAndUseful(UserContext.getUserId(), EntityTypeEnum.QUESTION, requestParam.getId(), requestParam.getEntityUserId(), UserActionTypeEnum.LIKE);
    }

    @Override
    public void resolvedQuestion(QuestionSolveReqDTO requestParam) {
        checkQuestionExist(requestParam.getId());
        checkQuestionOwner(requestParam.getId());

        QuestionDO question = baseMapper.selectById(requestParam.getId());
        question.setSolvedFlag(1);
        baseMapper.updateById(question);
        //todo 用户解决问题数如何去定义
    }

    @Override
    public QuestionRespDTO findQuestionById(Long id) {
        String question = stringRedisTemplate.opsForValue().get(QUESTION_CONTENT_KEY + id);
        Random random = new Random();
        QuestionDO questionDO = null;
        if (StrUtil.isBlank(question)) {
            RLock lock = redissonClient.getLock(QUESTION_LOCK_KEY + id + random.nextInt(6));
            lock.lock();
            try {
                question = stringRedisTemplate.opsForValue().get(QUESTION_CONTENT_KEY + id);
                if (StrUtil.isBlank(question)) {
                    questionDO = baseMapper.selectById(id);
                    if (questionDO == null || questionDO.getDelFlag() != 0) {
                        throw new ClientException(QUESTION_NULL);
                    }
                    question = JSON.toJSONString(questionDO);
                    stringRedisTemplate.opsForValue().set(QUESTION_CONTENT_KEY + id, question, 1, TimeUnit.HOURS);
                } else {
                    questionDO = BeanUtil.toBean(BeanUtil.toBean(question, HashMap.class), QuestionDO.class);
                }
            } finally {
                lock.unlock();
            }
        }

        QuestionRespDTO result = new QuestionRespDTO();
        BeanUtils.copyProperties(questionDO, result);

        Long userId = UserContext.getUserId();

        // 更新最后一次浏览时间
        userActionService.updateLastViewTime(userId, EntityTypeEnum.QUESTION, id);

        String likeStatus = UserContext.getUsername() == null ? "未登录" : userActionService.getUserAction(userId, EntityTypeEnum.QUESTION, id).getLikeStat() == 1 ?  "已点赞" : "未点赞";
        result.setLikeStatus(likeStatus);
        String collectStatus = UserContext.getUsername() == null ? "未登录" : userActionService.getUserAction(userId, EntityTypeEnum.QUESTION, id).getCollectStat() == 1 ? "已收藏" : "未收藏";
        result.setCollectStatus(collectStatus);

        // 填充计数信息
        result.setLikeCount(redisCount.hGet(QUESTION_COUNT_KEY + id, "like"));
        result.setViewCount(redisCount.hGet(QUESTION_COUNT_KEY + id, "view"));
        result.setCommentCount(redisCount.hGet(QUESTION_COUNT_KEY + id, "comment"));

        // 更新浏览数
        redisCount.hIncr(QUESTION_COUNT_KEY + id, "view", 1);
        return result;
    }

    @Override
    public IPage<QuestionPageRespDTO> pageMyQuestion(QuestionMinePageReqDTO requestParam) {
        LambdaQueryWrapper<QuestionDO> queryWrapper = Wrappers.lambdaQuery(QuestionDO.class)
                .eq(QuestionDO::getDelFlag, 0)
                .eq(QuestionDO::getUserId, UserContext.getUserId());
        IPage<QuestionDO> page = baseMapper.selectPage(requestParam, queryWrapper);
        return page.convert(each -> BeanUtil.toBean(each, QuestionPageRespDTO.class));
    }

    @Override
    public List<QuestionPageRespDTO> findHotQuestion(Long categoryId) {
        Set<String> topQuestions = stringRedisTemplate.opsForZSet().reverseRange(HOT_QUESTION_KEY + categoryId, 0, 9);
        if (topQuestions.isEmpty()) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<QuestionDO>()
                        .in(QuestionDO::getId, topQuestions)
                        .eq(QuestionDO::getDelFlag, 0))
                .stream()
                .map(each -> {
                    QuestionPageRespDTO dto = BeanUtil.toBean(each, QuestionPageRespDTO.class);
                    fillQuestionCount(dto);
                    return dto;
                }).toList();
    }

    @Override
    public void collectQuestion(QuestionCollectReqDTO requestParam) {
        checkQuestionExist(requestParam.getId());

        userActionService.collectAndLikeAndUseful(UserContext.getUserId(), EntityTypeEnum.QUESTION, requestParam.getId(), requestParam.getEntityUserId(), UserActionTypeEnum.COLLECT);
    }

    @Override
    public IPage<QuestionPageRespDTO> pageCollectQuestion(QuestionCollectPageReqDTO requestParam) {
        Long userId = UserContext.getUserId();
        Page<QuestionPageRespDTO> page = new Page<>(requestParam.getCurrent(), requestParam.getSize());
        return userActionMapper.pageCollectQuestion(page, userId, requestParam).convert(
                each -> {
                    fillQuestionCount(each);
                    return each;
                }
        );
    }

    @Override
    public IPage<QuestionPageRespDTO> pageRecentViewQuestion(QuestionRecentPageReqDTO requestParam) {
        Long userId = UserContext.getUserId();
        Page<QuestionPageRespDTO> page = new Page<>(requestParam.getCurrent(), requestParam.getSize());
        return userActionMapper.pageRecentViewQuestion(page, userId, requestParam).convert(
                each -> {
                    fillQuestionCount(each);
                    return each;
                }
        );
    }

    private void fillQuestionCount(QuestionPageRespDTO dto) {
        dto.setLikeCount(redisCount.hGet(QUESTION_COUNT_KEY + dto.getId(), "like"));
        dto.setViewCount(redisCount.hGet(QUESTION_COUNT_KEY + dto.getId(), "view"));
        dto.setCommentCount(redisCount.hGet(QUESTION_COUNT_KEY + dto.getId(), "comment"));
    }

    public void checkQuestionExist(Long id) {
        QuestionDO question = baseMapper.selectById(id);
        if (question == null || question.getDelFlag() != 0) {
            throw new ClientException(QUESTION_NULL);
        }
    }

    public void checkQuestionOwner(Long id) {
        QuestionDO question = baseMapper.selectById(id);
        long userId = UserContext.getUserId();
        if (!question.getUserId().equals(userId)) {
            throw new ClientException(QUESTION_ACCESS_CONTROL_ERROR);
        }
    }

    public Long getUserIdByQuestionId(Long id) {
        QuestionDO question = baseMapper.selectById(id);
        return question.getUserId();

    }

    public void dataSync(QuestionDO questionDO, MessageTypeEnum messageType) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("question", questionDO);
        MqEvent event = MqEvent.builder()
                .messageType(messageType)
                .data(map)
                .build();
        producer.dataSync(event);
    }

}
