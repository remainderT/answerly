package org.buaa.project.dto.req;

import lombok.Data;

/**
 * 评论点赞请求参数
 */
@Data
public class CommentLikeReqDTO {

    /**
     * 评论id
     */
    private Long id;

    /**
     * 评论拥有者id
     */
    private Long entityUserId;
}
