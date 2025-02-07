package org.buaa.project.dto.req;

import lombok.Data;

/**
 * 收藏问题请求参数
 */
@Data
public class QuestionCollectReqDTO {

    /**
     * 问题id
     */
    private Long id;

    /**
     * 是否收藏
     */
    private Integer isCollect;
}
