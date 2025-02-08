package org.buaa.project.dto.resp;

import lombok.Data;

import java.util.Date;

/**
 * 问题分页查询响应
 */
@Data
public class QuestionPageRespDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 是否解决 0：未解决 1：已解决
     */
    private Integer solvedFlag;

    /**
     * 日期
     */
    private Date createTime;

}