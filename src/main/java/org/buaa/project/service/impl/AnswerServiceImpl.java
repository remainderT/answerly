package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import org.buaa.project.dao.entity.AnswerDO;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.AnswerMapper;
import org.buaa.project.dto.req.AnswerMinePageReqDTO;
import org.buaa.project.dto.req.AnswerPageReqDTP;
import org.buaa.project.dto.req.AnswerUpdateReqDTO;
import org.buaa.project.dto.req.AnswerUploadReqDTO;
import org.buaa.project.dto.resp.AnswerPageRespDTO;
import org.buaa.project.service.AnswerService;
import org.buaa.project.service.LikeService;
import org.buaa.project.service.QuestionService;
import org.buaa.project.toolkit.CustomIdGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.buaa.project.common.consts.RedisCacheConstants.USER_INFO_KEY;
import static org.buaa.project.common.enums.QAErrorCodeEnum.ANSWER_ACCESS_CONTROL_ERROR;
import static org.buaa.project.common.enums.QAErrorCodeEnum.ANSWER_NULL;

/**
 * 回答接口实现层
 */
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, AnswerDO> implements AnswerService {

    private final QuestionService questionService;

    private final StringRedisTemplate stringRedisTemplate;

    private final LikeService likeService;

    @Override
    public void likeAnswer(long id, long entityUserId){
        checkAnswerExist(id);

        long userId = UserContext.getUserId();
        likeService.like(userId, EntityTypeEnum.ANSWER, id, entityUserId);
    }

    @Override
    public void uploadAnswer(AnswerUploadReqDTO reqDTO){
        long userId = UserContext.getUserId();
        AnswerDO answerDO = BeanUtil.copyProperties(reqDTO, AnswerDO.class);
        answerDO.setUserId(userId);
        answerDO.setUsername(UserContext.getUsername());
        answerDO.setId(CustomIdGenerator.getId());
        baseMapper.insert(answerDO);
    }

    @Override
    public void deleteAnswer(long id){
        checkAnswerExist(id);
        if(!UserContext.getUserType().equals("admin")){
            checkAnswerOwner(id);
        }

        AnswerDO answerDO = baseMapper.selectById(id);
        answerDO.setDelFlag(1);
        baseMapper.updateById(answerDO);
    }

    @Override
    public void markUsefulAnswer(long id){
        checkAnswerExist(id);

        AnswerDO answerDO = baseMapper.selectById(id);
        questionService.checkQuestionOwner(answerDO.getQuestionId());
        answerDO.setUseful(1);
        baseMapper.updateById(answerDO);
    }

    @Override
    public void updateAnswer(AnswerUpdateReqDTO requestParam) {
        //todo 也许可以加一个被标为有用之后就不允许修改的功能
        checkAnswerExist(requestParam.getId());
        checkAnswerOwner(requestParam.getId());

        LambdaUpdateWrapper<AnswerDO> queryWrapper = Wrappers.lambdaUpdate(AnswerDO.class)
                .eq(AnswerDO::getId, requestParam.getId());
        AnswerDO answerDO = baseMapper.selectOne(queryWrapper);
        BeanUtils.copyProperties(requestParam, answerDO);
        baseMapper.update(answerDO, queryWrapper);
    }

    @Override
    public IPage<AnswerPageRespDTO> pageMyAnswer(AnswerMinePageReqDTO requestParam){
        LambdaQueryWrapper<AnswerDO> queryWrapper = Wrappers.lambdaQuery(AnswerDO.class)
                .eq(AnswerDO::getDelFlag, 0)
                .eq(AnswerDO::getUserId, UserContext.getUserId());
        IPage<AnswerDO> page = baseMapper.selectPage(requestParam, queryWrapper);

        List<AnswerPageRespDTO> answerPageRespDTOList = page.getRecords().stream().map(answerDO -> {
            String username = answerDO.getUsername();
            String userJson = stringRedisTemplate.opsForValue().get(USER_INFO_KEY + username);
            UserDO userDO = JSON.parseObject(userJson, UserDO.class);
            AnswerPageRespDTO answerPageRespDTO = BeanUtil.copyProperties(answerDO, AnswerPageRespDTO.class);
            QuestionDO questionDO = questionService.getById(answerDO.getQuestionId());
            answerPageRespDTO.setTitle(questionDO.getTitle());
            answerPageRespDTO.setAvatar(userDO.getAvatar());
            answerPageRespDTO.setLikeCount(likeService.findEntityLikeCount(EntityTypeEnum.ANSWER, answerDO.getId()));
            answerPageRespDTO.setLikeStatus(UserContext.getUsername() == null ? "未登录" : likeService.findEntityLikeStatus(UserContext.getUserId(), EntityTypeEnum.ANSWER, answerDO.getId()));
            return answerPageRespDTO;
        }).collect(Collectors.toList());

        IPage<AnswerPageRespDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(answerPageRespDTOList);

        return result;
    }
    @Override
    public IPage<AnswerPageRespDTO> pageAnswer(AnswerPageReqDTP requestParam) {
        LambdaQueryWrapper<AnswerDO> queryWrapper = Wrappers.lambdaQuery(AnswerDO.class)
                .eq(AnswerDO::getDelFlag, 0)
                .eq(AnswerDO::getQuestionId, requestParam.getId());
        IPage<AnswerDO> page = baseMapper.selectPage(requestParam, queryWrapper);

        List<AnswerPageRespDTO> answerPageRespDTOList = page.getRecords().stream().map(answerDO -> {
            String username = answerDO.getUsername();
            String userJson = stringRedisTemplate.opsForValue().get(USER_INFO_KEY + username);
            UserDO userDO = JSON.parseObject(userJson, UserDO.class);
            AnswerPageRespDTO answerPageRespDTO = BeanUtil.copyProperties(answerDO, AnswerPageRespDTO.class);
            QuestionDO questionDO = questionService.getById(answerDO.getQuestionId());
            answerPageRespDTO.setTitle(questionDO.getTitle());
            answerPageRespDTO.setAvatar(userDO.getAvatar());
            answerPageRespDTO.setLikeCount(likeService.findEntityLikeCount(EntityTypeEnum.ANSWER, answerDO.getId()));
            answerPageRespDTO.setLikeStatus(UserContext.getUsername() == null ? "未登录" : likeService.findEntityLikeStatus(UserContext.getUserId(), EntityTypeEnum.ANSWER, answerDO.getId()));
            return answerPageRespDTO;
        }).collect(Collectors.toList());

        IPage<AnswerPageRespDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(answerPageRespDTOList);

        return result;
    }

    @Override
    public void checkAnswerExist(long id) {
        AnswerDO answer = baseMapper.selectById(id);
        if (answer == null || answer.getDelFlag() != 0) {
            throw new ClientException(ANSWER_NULL);
        }
    }

    @Override
    public void checkAnswerOwner(long id) {
        AnswerDO answer = baseMapper.selectById(id);
        long userId = UserContext.getUserId();
        if (!answer.getUserId().equals(userId)) {
            throw new ClientException(ANSWER_ACCESS_CONTROL_ERROR);
        }
    }


}
