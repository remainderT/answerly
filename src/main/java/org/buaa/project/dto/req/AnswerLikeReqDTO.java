package org.buaa.project.dto.req;

import lombok.Data;

/**
 * 回答点赞请求参数
 */
@Data
public class AnswerLikeReqDTO {

    /**
     * 回答id
     */
    private Long id;

    /**
     * 回答拥有者id
     */
    private Long entityUserId;
}
