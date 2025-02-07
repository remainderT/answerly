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
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dao.mapper.UserActionMapper;
import org.buaa.project.dto.req.QuestionCollectPageReqDTO;
import org.buaa.project.dto.req.QuestionCollectReqDTO;
import org.buaa.project.dto.req.QuestionLikeReqDTO;
import org.buaa.project.dto.req.QuestionMinePageReqDTO;
import org.buaa.project.dto.req.QuestionPageReqDTO;
import org.buaa.project.dto.req.QuestionUpdateReqDTO;
import org.buaa.project.dto.req.QuestionUploadReqDTO;
import org.buaa.project.dto.resp.QuestionPageRespDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.mq.MqProducer;
import org.buaa.project.service.LikeService;
import org.buaa.project.service.QuestionService;
import org.buaa.project.toolkit.CustomIdGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

import static org.buaa.project.common.enums.QAErrorCodeEnum.QUESTION_ACCESS_CONTROL_ERROR;
import static org.buaa.project.common.enums.QAErrorCodeEnum.QUESTION_NULL;

/**
 * The type Question service.
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, QuestionDO> implements QuestionService {

    private final LikeService likeService;

    private final MqProducer producer;

    private final UserActionMapper userActionMapper;

    @Override
    public void uploadQuestion(QuestionUploadReqDTO requestParam) {
        QuestionDO question = BeanUtil.toBean(requestParam, QuestionDO.class);
        question.setUserId(UserContext.getUserId());
        question.setUsername(UserContext.getUsername());
        question.setId(CustomIdGenerator.getId());
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
        checkQuestionExist(requestParam.getQuestionId());

        long userId = UserContext.getUserId();
        likeService.like(userId, EntityTypeEnum.QUESTION, requestParam.getQuestionId(), requestParam.getEntityUserId());
    }

    @Override
    public void resolvedQuestion(Long id) {
        checkQuestionExist(id);
        checkQuestionOwner(id);

        QuestionDO question = baseMapper.selectById(id);
        question.setSolvedFlag(1);
        baseMapper.updateById(question);
    }

    @Override
    public QuestionRespDTO findQuestionById(Long id) {
        QuestionDO question = baseMapper.selectById(id);
        QuestionRespDTO result = new QuestionRespDTO();
        BeanUtils.copyProperties(question, result);
        int likeCount = likeService.findEntityLikeCount(EntityTypeEnum.QUESTION, id);
        Long userId = UserContext.getUserId();
        String likeStatus = UserContext.getUsername() == null ? "未登录" : likeService.findEntityLikeStatus(userId, EntityTypeEnum.QUESTION, id);
        result.setLikeCount(likeCount);
        result.setLikeStatus(likeStatus);

        // 更新用户最后一次浏览时间
        userActionMapper.updateLastViewTime(userId, id);
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
    public List<QuestionPageRespDTO> findHotQuestion(Integer category) {
        //todo 修改热度值计算，暂时先根据like_count和view_count排序
        LambdaQueryWrapper<QuestionDO> wrapper = Wrappers.lambdaQuery(QuestionDO.class)
                .eq(QuestionDO::getCategoryId, category)
                .orderByDesc(QuestionDO::getLikeCount)
                .orderByDesc(QuestionDO::getViewCount)
                .last("LIMIT 10");
        List<QuestionDO> questionDOList = baseMapper.selectList(wrapper);

        return questionDOList.stream()
                .map(questionDO -> {
                    QuestionPageRespDTO dto = new QuestionPageRespDTO();
                    BeanUtils.copyProperties(questionDO, dto);
                    return dto;
                })
                .toList();
    }

    @Override
    public void collectQuestion(QuestionCollectReqDTO requestParam) {
        checkQuestionExist(requestParam.getId());
        Long userId = UserContext.getUserId();
        userActionMapper.collectQuestion(requestParam.getId(), userId, requestParam.getIsCollect());
    }

    @Override
    public IPage<QuestionPageRespDTO> pageCollectQuestion(QuestionCollectPageReqDTO requestParam) {
        Long userId = UserContext.getUserId();
        Page<QuestionPageRespDTO> page = new Page<>(requestParam.getCurrent(), requestParam.getSize());
        return userActionMapper.pageCollectQuestion(page, userId, requestParam);
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

}
