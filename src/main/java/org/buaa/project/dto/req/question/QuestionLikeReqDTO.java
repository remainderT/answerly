package org.buaa.project.dto.req.question;

import lombok.Data;

/**
 * 点赞题目请求参数
 */
@Data
public class QuestionLikeReqDTO {

    /**
     * 题目id
     */
    private Long id;

    /**
     * 题目拥有者id
     */
    private Long entityUserId;
}
