package org.buaa.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.buaa.project.common.database.BaseDO;
import org.buaa.project.toolkit.sensitive.SensitiveField;

@Data
@TableName("comment")
public class CommentDO extends BaseDO {
    /**
     * ID - unique identifier for each comment
     */
    private Long id;

    /**
     * user_id - the ID of the user who posted the comment
     */
    private Long userId;

    /**
     * username - the username of the user who posted the comment
     */
    private String username;

    /**
     * question_id - the ID of the question this comment responds to
     */
    private Long questionId;

    /**
     * top_comment_id - the ID of the top comment, if this comment is a reply to a comment
     */
    private Long topCommentId;

    /**
     *  parent_comment_id - the ID of the parent comment, if this comment is a reply to a comment
     */
    private Long parentCommentId;

    /**
     * content - the content of the comment, with a maximum length of 1024 characters
     */
    @SensitiveField
    private String content;

    /**
     * images - paths to images associated with the comment, separated by commas, with a maximum of 9 images
     */
    private String images;

    /**
     * like_count - the count of likes this comment has received, default is 0
     */
    private Integer likeCount;

    /**
     * useful - indicates if the comment is marked as useful, 1 for true, 0 for false
     */
    private Integer useful;

}
