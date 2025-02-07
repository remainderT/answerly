package org.buaa.project.dto.resp;

import lombok.Data;

import java.util.Date;

/**
 * 回答分页查询响应
 */
@Data
public class AnswerPageRespDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;

    /**
     * 图片
     */
    private String images;

    /**
     * username
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 点赞状态
     */
    private String likeStatus;

    /**
     * 是否被采纳 0：未采纳 1：已采纳
     */
    private Integer useful;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 问题id
     */
    private Long questionId;

    /**
     * 问题标题
     */
    private String title;
}
