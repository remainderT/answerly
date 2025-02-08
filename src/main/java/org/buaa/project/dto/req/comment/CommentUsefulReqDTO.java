package org.buaa.project.dto.req.comment;

import lombok.Data;

/**
 * 问题有用请求参数
 */
@Data
public class CommentUsefulReqDTO {

    /**
     * 评论id
     */
    private Long id;

    /**
     * 是否有用
     */
    private Integer isUseful;
}
