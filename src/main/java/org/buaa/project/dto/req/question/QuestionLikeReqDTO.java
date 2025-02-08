package org.buaa.project.dto.req.question;

import lombok.Data;

/**
 * 问题点赞请求参数
 */
@Data
public class QuestionLikeReqDTO {

    /**
     * 问题id
     */
    private Long questionId;

    /**
     * 问题拥有者id
     */
    private Long entityUserId;

    /**
     * 是否点赞
     */
    private Integer isLike;
}
