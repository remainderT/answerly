package org.buaa.project.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 题目分页查询响应
 */
@Data
@Builder
public class QuestionPageAllRespDTO {

    Long total;

    Long size;

    Long current;

    List<QuestionPageRespDTO> records;

}
