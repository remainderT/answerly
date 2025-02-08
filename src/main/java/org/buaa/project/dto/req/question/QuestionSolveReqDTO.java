package org.buaa.project.dto.req.question;

import lombok.Data;

/**
 * 问题解决请求参数
 */
@Data
public class QuestionSolveReqDTO {

    /**
     * 问题id
     */
    private Long id;

    /**
     * 是否解决
     */
    private Integer isSolve;
}
