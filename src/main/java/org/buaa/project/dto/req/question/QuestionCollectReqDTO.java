package org.buaa.project.dto.req.question;

import lombok.Data;

/**
 * 收藏题目请求参数
 */
@Data
public class QuestionCollectReqDTO {

    /**
     * 题目id
     */
    private Long id;

    /**
     * 题目所有者id
     */
    private Long entityUserId;
}
