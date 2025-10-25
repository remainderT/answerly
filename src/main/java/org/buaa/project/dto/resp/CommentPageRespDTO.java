package org.buaa.project.dto.resp;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 评论分页查询响应
 */
@Data
public class CommentPageRespDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;

    /**
     * 父评论 id
     */
    private Long parentCommentId;

    /**
     * 顶级评论 id
     */
    private Long topCommentId;

    /**
     * 图片
     */
    private String images;

    /**
     * username
     */
    private String username;

    /**
     * usertype
     */
    private String usertype;

    /**
     * 头像
     */
    private String avatar;

    /**
     * parentCommentId 所属者姓名
     */
    private String commentTo;

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
     * 子评论
     */
    private List<CommentPageRespDTO> childComments;

    /**
     * 问题id
     */
    private Long questionId;
}
