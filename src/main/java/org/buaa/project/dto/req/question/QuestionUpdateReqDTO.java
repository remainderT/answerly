package org.buaa.project.dto.req.question;

import lombok.Data;

/**
 * 问题修改请求参数
 */
@Data
public class QuestionUpdateReqDTO {

    /**
     * 需要修改的问题id
     */
    private Long id;

    /**
     * 包含的图片
     */
    private String images;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;
}
