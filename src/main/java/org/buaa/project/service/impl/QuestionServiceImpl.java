package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.exception.ServiceException;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dto.req.QuestionFindReqDTO;
import org.buaa.project.dto.req.QuestionUploadReqDTO;
import org.buaa.project.dto.resp.QuestionBriefRespDTO;
import org.buaa.project.dto.resp.QuestionRespDTO;
import org.buaa.project.service.QuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Question service.
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, QuestionDO> implements QuestionService {

    /**
     * 上传题目
     *
     * @param params
     * @return
     */
    @Transactional
    @Override
    public Boolean uploadQuestion(QuestionUploadReqDTO params) {
        //todo 验证category和idUser是否在category和user表中存在
        int inserted = baseMapper.insert(BeanUtil.toBean(params, QuestionDO.class));
        boolean isSavedQuestion = inserted > 0;
        if (params.getPictures() != null && !params.getPictures().isEmpty()) {
            //todo 进行图片处理
        }
        return isSavedQuestion;
    }

    /**
     * 删除题目
     *
     * @param id
     * @return
     */
    @Transactional
    @Override
    public Boolean deleteQuestion(Long id) {
//        int res = baseMapper.deleteById(id);
//        return res > 0;
        QuestionDO question = new QuestionDO();
        question.setId(id);
        question.setDelFlag(1);
        int result = baseMapper.updateById(question);
        return result > 0;
    }

    /**
     * 点赞题目
     *
     * @param id
     * @return
     */
    @Transactional
    @Override
    public Boolean likeQuestion(Long id) {
        QuestionDO question = baseMapper.selectById(id);
        if (question == null) {
            throw new ServiceException("问题不存在");
        }
        int curLikeCount = question.getLikeCount();
        question.setLikeCount(curLikeCount + 1);
        int result = baseMapper.updateById(question);
        return result > 0;
    }

    /**
     * 查找问题
     *
     * @param params
     * @return
     */
    @Transactional
    @Override
    public List<QuestionBriefRespDTO> findQuestion(QuestionFindReqDTO params) {
        LambdaQueryWrapper<QuestionDO> wrapper;
        if (params.getSolvedFlag() == 2) {
            wrapper = Wrappers.lambdaQuery(QuestionDO.class)
                    .eq(QuestionDO::getCategory, params.getCategory());
        } else {
            wrapper = Wrappers.lambdaQuery(QuestionDO.class)
                    .eq(QuestionDO::getCategory, params.getCategory())
                    .eq(QuestionDO::getSolvedFlag, params.getSolvedFlag());
        }
        Page<QuestionDO> questionPage = new Page<>(params.getPage(), 10);

        Page<QuestionDO> resultPage = baseMapper.selectPage(questionPage, wrapper);
        List<QuestionBriefRespDTO> responseList = resultPage.getRecords().stream()
                .map(questionDO -> {
                    QuestionBriefRespDTO dto = new QuestionBriefRespDTO();
                    BeanUtils.copyProperties(questionDO, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        return responseList;
    }

    /**
     * 查找热门10道题
     *
     * @param category
     * @return
     */
    @Transactional
    @Override
    public List<QuestionBriefRespDTO> findHotQuestion(int category) {
        //todo 修改热度值计算，暂时先根据like_count和view_count排序
        LambdaQueryWrapper<QuestionDO> wrapper = Wrappers.lambdaQuery(QuestionDO.class)
                .eq(QuestionDO::getCategory, category)
                .orderByDesc(QuestionDO::getLikeCount)
                .orderByDesc(QuestionDO::getViewCount)
                .last("LIMIT 10");
        List<QuestionDO> questionDOList = baseMapper.selectList(wrapper);

        return questionDOList.stream()
                .map(questionDO -> {
                    QuestionBriefRespDTO dto = new QuestionBriefRespDTO();
                    BeanUtils.copyProperties(questionDO, dto);
                    return dto;
                })
                .toList();
    }

    /**
     * 根据id获取题目信息
     * @param id
     * @return
     */
    @Transactional
    @Override
    public QuestionRespDTO findQuestionById(Long id) {
        QuestionDO question = baseMapper.selectById(id);
        QuestionRespDTO result = new QuestionRespDTO();
        BeanUtils.copyProperties(question, result);
        return result;
    }

}
