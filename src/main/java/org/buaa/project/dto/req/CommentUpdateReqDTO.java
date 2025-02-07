package org.buaa.project.dto.req;

import lombok.Data;

/**
 * 评论更新请求参数
 */
@Data
public class CommentUpdateReqDTO {

    /**
     * id
     */
    private Long id;

    /**
     * content - the content of the answer, with a maximum length of 1024 characters
     */
    private String content;

    /**
     * images - paths to images associated with the answer, separated by commas, with a maximum of 9 images
     */
    private String images;
}
