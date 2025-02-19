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
import org.buaa.project.common.enums.MessageTypeEnum;
import org.buaa.project.common.enums.UserActionTypeEnum;
import org.buaa.project.dao.entity.CommentDO;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.CommentMapper;
import org.buaa.project.dto.req.comment.CommentLikeReqDTO;
import org.buaa.project.dto.req.comment.CommentMinePageReqDTO;
import org.buaa.project.dto.req.comment.CommentPageReqDTP;
import org.buaa.project.dto.req.comment.CommentUpdateReqDTO;
import org.buaa.project.dto.req.comment.CommentUploadReqDTO;
import org.buaa.project.dto.req.comment.CommentUsefulReqDTO;
import org.buaa.project.dto.resp.CommentPageRespDTO;
import org.buaa.project.mq.MqEvent;
import org.buaa.project.mq.MqProducer;
import org.buaa.project.service.CommentService;
import org.buaa.project.service.QuestionService;
import org.buaa.project.service.UserActionService;
import org.buaa.project.toolkit.RedisCount;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.buaa.project.common.consts.RedisCacheConstants.COMMENT_COUNT_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.QUESTION_COUNT_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.USER_COUNT_KEY;
import static org.buaa.project.common.consts.RedisCacheConstants.USER_INFO_KEY;
import static org.buaa.project.common.enums.QAErrorCodeEnum.COMMENT_ACCESS_CONTROL_ERROR;
import static org.buaa.project.common.enums.QAErrorCodeEnum.COMMENT_NULL;

/**
 * 评论接口实现层
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, CommentDO> implements CommentService {

    private final QuestionService questionService;

    private final StringRedisTemplate stringRedisTemplate;

    private final UserActionService userActionService;

    private final RedisCount redisCount;

    private final MqProducer producer;

    @Override
    public void likeComment(CommentLikeReqDTO requestParam) {
        checkCommentExist(requestParam.getId());

        userActionService.collectAndLikeAndUseful(UserContext.getUserId(), EntityTypeEnum.COMMENT, requestParam.getId(), requestParam.getEntityUserId(), UserActionTypeEnum.LIKE);
    }

    @Override
    public void uploadComment(CommentUploadReqDTO requestParam) {
        CommentDO commentDO = BeanUtil.copyProperties(requestParam, CommentDO.class);
        Long questionId = commentDO.getQuestionId();
        questionService.checkQuestionExist(questionId);

        commentDO.setUserId(UserContext.getUserId());
        commentDO.setUsername(UserContext.getUsername());

        baseMapper.insert(commentDO);
        redisCount.hIncr(QUESTION_COUNT_KEY + questionId, "comment", 1);

        afterComment(commentDO, true);
    }

    @Override
    public void deleteComment(Long id) {
        checkCommentExist(id);
        if(!UserContext.getUserType().equals("admin")) {
            checkCommentOwner(id);
        }

        CommentDO commentDO = baseMapper.selectById(id);
        commentDO.setDelFlag(1);
        baseMapper.updateById(commentDO);

        int likeCount = redisCount.hGet(COMMENT_COUNT_KEY + commentDO.getUserId(), "like");
        redisCount.hIncr(USER_COUNT_KEY + commentDO.getUserId(), "like", -likeCount);
        redisCount.hIncr(QUESTION_COUNT_KEY + commentDO.getQuestionId(), "comment", -1);

        afterComment(commentDO, false);
    }

    @Override
    public void markUsefulComment(CommentUsefulReqDTO requestParam) {
        checkCommentExist(requestParam.getId());

        CommentDO commentDO = baseMapper.selectById(requestParam.getId());
        questionService.checkQuestionOwner(commentDO.getQuestionId());
        boolean isPositive = commentDO.getUseful() == 0;
        commentDO.setUseful(isPositive ? 1 : 0);
        baseMapper.updateById(commentDO);

        userActionService.collectAndLikeAndUseful(UserContext.getUserId(), EntityTypeEnum.COMMENT, requestParam.getId(), commentDO.getUserId(), UserActionTypeEnum.USEFUL);

    }

    @Override
    public void updateComment(CommentUpdateReqDTO requestParam) {
        checkCommentExist(requestParam.getId());
        checkCommentOwner(requestParam.getId());

        LambdaUpdateWrapper<CommentDO> queryWrapper = Wrappers.lambdaUpdate(CommentDO.class)
                .eq(CommentDO::getId, requestParam.getId());
        CommentDO CommentDO = baseMapper.selectOne(queryWrapper);
        BeanUtils.copyProperties(requestParam, CommentDO);
        baseMapper.update(CommentDO, queryWrapper);
    }

    @Override
    public IPage<CommentPageRespDTO> pageMyComment(CommentMinePageReqDTO requestParam) {
        LambdaQueryWrapper<CommentDO> queryWrapper = Wrappers.lambdaQuery(CommentDO.class)
                .eq(CommentDO::getDelFlag, 0)
                .eq(CommentDO::getUserId, UserContext.getUserId());
        IPage<CommentDO> page = baseMapper.selectPage(requestParam, queryWrapper);

        List<CommentPageRespDTO> CommentPageRespDTOList = page.getRecords().stream().map(commentDO -> {
            String username = commentDO.getUsername();
            String userJson = stringRedisTemplate.opsForValue().get(USER_INFO_KEY + username);
            UserDO userDO = JSON.parseObject(userJson, UserDO.class);
            CommentPageRespDTO CommentPageRespDTO = BeanUtil.copyProperties(commentDO, CommentPageRespDTO.class);
            CommentPageRespDTO.setAvatar(userDO.getAvatar());
            CommentPageRespDTO.setLikeCount(redisCount.hGet(COMMENT_COUNT_KEY + commentDO.getId(), "like"));
            String likeStatus = userActionService.getUserAction(UserContext.getUserId(), EntityTypeEnum.QUESTION, commentDO.getId()).getLikeStat() == 1 ?  "已点赞" : "未点赞";
            CommentPageRespDTO.setLikeStatus(likeStatus);
            return CommentPageRespDTO;
        }).collect(Collectors.toList());

        IPage<CommentPageRespDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(CommentPageRespDTOList);

        return result;
    }

    @Override
    public IPage<CommentPageRespDTO> pageComment(CommentPageReqDTP requestParam) {
        // 先查出文章的顶级评论（parent_comment_id = 0）
        LambdaQueryWrapper<CommentDO> queryWrapper = Wrappers.lambdaQuery(CommentDO.class)
                .eq(CommentDO::getDelFlag, 0)
                .eq(CommentDO::getQuestionId, requestParam.getId())
                .eq(CommentDO::getParentCommentId, 0);
        IPage<CommentDO> page = baseMapper.selectPage(requestParam, queryWrapper);

        List<CommentPageRespDTO> CommentPageRespDTOList = page.getRecords().stream().map(top -> {
            // 接下来就是针对每个顶级评论，查询它下面的所有回复
            LambdaQueryWrapper<CommentDO> subQueryWrapper = Wrappers.lambdaQuery(CommentDO.class)
                    .eq(CommentDO::getDelFlag, 0)
                    .eq(CommentDO::getTopCommentId, top.getId());
            List<CommentDO> childComments = baseMapper.selectList(subQueryWrapper);
            List<CommentPageRespDTO> childCommentDTOs = childComments.stream()
                    .map(child -> fillCommentDetails(child, childComments))
                    .toList();
            CommentPageRespDTO commentPageRespDTO = fillCommentDetails(top, null);
            BeanUtil.copyProperties(top, CommentPageRespDTO.class);
            commentPageRespDTO.setChildComments(childCommentDTOs);
            return commentPageRespDTO;
        }).collect(Collectors.toList());

        IPage<CommentPageRespDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(CommentPageRespDTOList);

        return result;
    }

    public CommentPageRespDTO fillCommentDetails(CommentDO commentDO, List<CommentDO> commentDOS) {
        String username = commentDO.getUsername();
        String userJson = stringRedisTemplate.opsForValue().get(USER_INFO_KEY + username);
        UserDO userDO = JSON.parseObject(userJson, UserDO.class);
        Optional<String> commentTo = Optional.empty();
        if (commentDOS != null && !commentDOS.isEmpty()) {
             commentTo = commentDOS.stream()
                    .filter(parent -> Objects.equals(parent.getId(), commentDO.getParentCommentId()))
                    .map(CommentDO::getUsername)
                    .findFirst();
        }
        CommentPageRespDTO commentPageRespDTO = BeanUtil.copyProperties(commentDO, CommentPageRespDTO.class);
        commentTo.ifPresent(commentPageRespDTO::setCommentTo);
        commentPageRespDTO.setAvatar(userDO.getAvatar());
        commentPageRespDTO.setLikeCount(redisCount.hGet(COMMENT_COUNT_KEY + commentDO.getId(), "like"));
        String likeStatus = UserContext.getUsername() == null ? "未登录" :
                userActionService.getUserAction(UserContext.getUserId(), EntityTypeEnum.COMMENT, commentDO.getId()).getLikeStat() == 1 ?  "已点赞" : "未点赞";
        commentPageRespDTO.setLikeStatus(likeStatus);
        return commentPageRespDTO;
    }

    public void afterComment(CommentDO commentDO, boolean isPositive) {
        Long entityUserId;
        EntityTypeEnum entityType;
        if (commentDO.getParentCommentId() == 0) {
            entityUserId = questionService.getUserIdByQuestionId(commentDO.getQuestionId());
            entityType = EntityTypeEnum.QUESTION;
        } else {
            entityUserId = baseMapper.selectById(commentDO.getParentCommentId()).getUserId();
            entityType = EntityTypeEnum.COMMENT;
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("isPositive", isPositive);
        data.put("comment", commentDO.getContent());

        MqEvent event = MqEvent.builder()
                .messageType(MessageTypeEnum.COMMENT)
                .entityType(entityType)
                .userId(commentDO.getUserId())
                .entityId(commentDO.getQuestionId())
                .entityUserId(entityUserId)
                .generateId(commentDO.getId())
                .data(data)
                .build();
        producer.messageSend(event);

    }

    @Override
    public void checkCommentExist(Long id) {
        CommentDO commentDO = baseMapper.selectById(id);
        if (commentDO == null || commentDO.getDelFlag() != 0) {
            throw new ClientException(COMMENT_NULL);
        }
    }

    @Override
    public void checkCommentOwner(Long id) {
        CommentDO commentDO = baseMapper.selectById(id);
        long userId = UserContext.getUserId();
        if (!commentDO.getUserId().equals(userId)) {
            throw new ClientException(COMMENT_ACCESS_CONTROL_ERROR);
        }
    }

}
