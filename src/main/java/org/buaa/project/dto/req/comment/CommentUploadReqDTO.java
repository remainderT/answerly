package org.buaa.project.dto.req.comment;

import lombok.Data;

/**
 * 评论上传请求参数
 */
@Data
public class CommentUploadReqDTO {

    /**
     * 问题ID
     */
    private Long questionId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID
     */
    private Long parentCommentId;

    /**
     * 顶级评论ID
     */
    private Long topCommentId;

    /**
     * 图片
     */
    private String images;
}
