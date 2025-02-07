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
import org.buaa.project.dao.entity.CommentDO;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.CommentMapper;
import org.buaa.project.dto.req.CommentLikeReqDTO;
import org.buaa.project.dto.req.CommentMinePageReqDTO;
import org.buaa.project.dto.req.CommentPageReqDTP;
import org.buaa.project.dto.req.CommentUpdateReqDTO;
import org.buaa.project.dto.req.CommentUploadReqDTO;
import org.buaa.project.dto.resp.CommentPageRespDTO;
import org.buaa.project.service.CommentService;
import org.buaa.project.service.LikeService;
import org.buaa.project.service.QuestionService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    private final LikeService likeService;

    @Override
    public void likeComment(CommentLikeReqDTO requestParam) {
        checkCommentExist(requestParam.getId());

        long userId = UserContext.getUserId();
        likeService.like(userId, EntityTypeEnum.COMMENT, requestParam.getId(), requestParam.getEntityUserId());
    }

    @Override
    public void uploadComment(CommentUploadReqDTO requestParam){
        CommentDO CommentDO = BeanUtil.copyProperties(requestParam, CommentDO.class);
        CommentDO.setUserId(UserContext.getUserId());
        CommentDO.setUsername(UserContext.getUsername());
        baseMapper.insert(CommentDO);
    }

    @Override
    public void deleteComment(Long id){
        checkCommentExist(id);
        if(!UserContext.getUserType().equals("admin")){
            checkCommentOwner(id);
        }

        CommentDO CommentDO = baseMapper.selectById(id);
        CommentDO.setDelFlag(1);
        baseMapper.updateById(CommentDO);
    }

    @Override
    public void markUsefulComment(Long id){
        checkCommentExist(id);

        CommentDO CommentDO = baseMapper.selectById(id);
        questionService.checkQuestionOwner(CommentDO.getQuestionId());
        CommentDO.setUseful(1);
        baseMapper.updateById(CommentDO);
    }

    @Override
    public void updateComment(CommentUpdateReqDTO requestParam) {
        //todo 也许可以加一个被标为有用之后就不允许修改的功能
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

        List<CommentPageRespDTO> CommentPageRespDTOList = page.getRecords().stream().map(CommentDO -> {
            String username = CommentDO.getUsername();
            String userJson = stringRedisTemplate.opsForValue().get(USER_INFO_KEY + username);
            UserDO userDO = JSON.parseObject(userJson, UserDO.class);
            CommentPageRespDTO CommentPageRespDTO = BeanUtil.copyProperties(CommentDO, CommentPageRespDTO.class);
            CommentPageRespDTO.setAvatar(userDO.getAvatar());
            CommentPageRespDTO.setLikeCount(likeService.findEntityLikeCount(EntityTypeEnum.COMMENT, CommentDO.getId()));
            CommentPageRespDTO.setLikeStatus(UserContext.getUsername() == null ? "未登录" : likeService.findEntityLikeStatus(UserContext.getUserId(), EntityTypeEnum.COMMENT, CommentDO.getId()));
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
        LambdaQueryWrapper<CommentDO> queryWrapper = Wrappers.lambdaQuery(CommentDO.class)
                .eq(CommentDO::getDelFlag, 0)
                .eq(CommentDO::getQuestionId, requestParam.getId())
                .eq(CommentDO::getParentCommentId, 0);
        IPage<CommentDO> page = baseMapper.selectPage(requestParam, queryWrapper);

        List<CommentPageRespDTO> CommentPageRespDTOList = page.getRecords().stream().map(top -> {
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
        commentPageRespDTO.setLikeCount(likeService.findEntityLikeCount(EntityTypeEnum.COMMENT, commentDO.getId()));
        commentPageRespDTO.setLikeStatus(UserContext.getUsername() == null ? "未登录" : likeService.findEntityLikeStatus(UserContext.getUserId(), EntityTypeEnum.COMMENT, commentDO.getId()));
        return commentPageRespDTO;
    }

    @Override
    public void checkCommentExist(Long id) {
        CommentDO Comment = baseMapper.selectById(id);
        if (Comment == null || Comment.getDelFlag() != 0) {
            throw new ClientException(COMMENT_NULL);
        }
    }

    @Override
    public void checkCommentOwner(Long id) {
        CommentDO Comment = baseMapper.selectById(id);
        long userId = UserContext.getUserId();
        if (!Comment.getUserId().equals(userId)) {
            throw new ClientException(COMMENT_ACCESS_CONTROL_ERROR);
        }
    }


}
