package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.common.convention.exception.ClientException;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.UserActionTypeEnum;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dao.mapper.UserActionMapper;
import org.buaa.project.dto.req.question.QuestionCollectPageReqDTO;
import org.buaa.project.dto.req.question.QuestionCollectReqDTO;
import org.buaa.project.dto.req.question.QuestionLikeReqDTO;
import org.buaa.project.dto.req.question.QuestionMinePageReqDTO;
import org.buaa.project.dto.req.question.QuestionPageReqDTO;
import org.buaa.project.dto.req.question.QuestionRecentPageReqDTO;
import org.buaa.project.dto.req.question.QuestionSolveReqDTO;
import org.buaa.project.dto.req.question.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.question.QuestionUploadReqDTO;
import org.buaa.project.dto.resp.QuestionPageRespDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.service.QuestionService;
import org.buaa.project.service.UserActionService;
import org.buaa.project.toolkit.RedisCount;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.buaa.project.common.consts.RedisCacheConstants.HOT_QUESTION_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_COUNT_KEY;
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

    @Override
    public void uploadQuestion(QuestionUploadReqDTO requestParam) {
        QuestionDO question = BeanUtil.toBean(requestParam, QuestionDO.class);
        question.setUserId(UserContext.getUserId());
        question.setUsername(UserContext.getUsername());
        baseMapper.insert(question);
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
        QuestionDO question = baseMapper.selectById(id);
        QuestionRespDTO result = new QuestionRespDTO();
        BeanUtils.copyProperties(question, result);

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
    public IPage<QuestionPageRespDTO> pageQuestion(QuestionPageReqDTO requestParam) {
        String search = requestParam.getSearch();
        LambdaQueryWrapper<QuestionDO> queryWrapper = Wrappers.lambdaQuery(QuestionDO.class)
                .eq(QuestionDO::getDelFlag, 0)
                .eq(requestParam.getSolvedFlag() != 2 , QuestionDO::getSolvedFlag, requestParam.getSolvedFlag())
                .eq(requestParam.getCategoryId() != null, QuestionDO::getCategoryId, requestParam.getCategoryId())
                .and(StringUtils.isNotBlank(search), wrapper ->
                        wrapper.like(QuestionDO::getTitle, search)
                                .or()
                                .like(QuestionDO::getContent, search)
                );

        IPage<QuestionDO> page = baseMapper.selectPage(requestParam, queryWrapper);
        return page.convert(each -> {
            QuestionPageRespDTO dto = BeanUtil.toBean(each, QuestionPageRespDTO.class);
            if (StringUtils.isNotBlank(search)) {
                // 处理高亮
                String highlightTemplate = "<mark>%s</mark>";
                String highlightSearch = String.format(highlightTemplate, search);
                if (StringUtils.isNotBlank(dto.getTitle())) {
                    dto.setTitle(dto.getTitle().replaceAll(Pattern.quote(search), highlightSearch));
                }
                if (StringUtils.isNotBlank(dto.getContent())) {
                    dto.setContent(dto.getContent().replaceAll(Pattern.quote(search), highlightSearch));
                }
            }
            fillQuestionCount(dto);
            return dto;
        });
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

}
