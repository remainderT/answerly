package org.buaa.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.buaa.project.common.database.BaseDO;
import org.buaa.project.toolkit.sensitive.SensitiveField;

@Data
@TableName("answer")
public class AnswerDO extends BaseDO {
    /**
     * ID - unique identifier for each answer
     */
    private Long id;

    /**
     * user_id - the ID of the user who posted the answer
     */
    private Long userId;

    /**
     * username - the username of the user who posted the answer
     */
    private String username;

    /**
     * question_id - the ID of the question this answer responds to
     */
    private Long questionId;

    /**
     * content - the content of the answer, with a maximum length of 1024 characters
     */
    @SensitiveField
    private String content;

    /**
     * images - paths to images associated with the answer, separated by commas, with a maximum of 9 images
     */
    private String images;

    /**
     * like_count - the count of likes this answer has received, default is 0
     */
    private Integer likeCount;

    /**
     * useful - indicates if the answer is marked as useful, 1 for true, 0 for false
     */
    private int useful;
}
