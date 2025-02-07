package org.buaa.project.dto.req;

import lombok.Data;

/**
 * Answer upload request DTO
 */
@Data
public class AnswerUploadReqDTO {

    /**
     * question_id - the ID of the question this answer responds to
     */
    private Long questionId;

    /**
     * content - the content of the answer, with a maximum length of 1024 characters
     */
    private String content;

    /**
     * images - paths to images associated with the answer, separated by commas, with a maximum of 9 images
     */
    private String images;
}
