package org.buaa.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.CommentDO;
import org.buaa.project.dto.req.comment.CommentLikeReqDTO;
import org.buaa.project.dto.req.comment.CommentMinePageReqDTO;
import org.buaa.project.dto.req.comment.CommentPageReqDTP;
import org.buaa.project.dto.req.comment.CommentUpdateReqDTO;
import org.buaa.project.dto.req.comment.CommentUploadReqDTO;
import org.buaa.project.dto.req.comment.CommentUsefulReqDTO;
import org.buaa.project.dto.resp.CommentPageRespDTO;

/**
 * 评论接口层
 */
public interface CommentService extends IService<CommentDO> {

    /**
     * 上传评论
     */
    void uploadComment(CommentUploadReqDTO requestParam);

    /**
     * 点赞评论
     */
    void likeComment(CommentLikeReqDTO requestParam);

    /**
     * 删除评论
     */
    void deleteComment(Long id);

    /**
     * 标记评论有用
     */
    void markUsefulComment(CommentUsefulReqDTO requestParam);

    /**
     * 更新评论
     */
    void updateComment(CommentUpdateReqDTO requestParam);

    /**
     * 检查评论是否存在
     */
    void checkCommentExist(Long id);

    /**
     * 检查评论所有者
     */
    void checkCommentOwner(Long id);

    /**
     * 分页查询评论
     */
    IPage<CommentPageRespDTO> pageComment(CommentPageReqDTP requestParam);

    /**
     * 分页查询我的评论
     */
    IPage<CommentPageRespDTO> pageMyComment(CommentMinePageReqDTO requestParam);


}
