package org.buaa.project.dto.req.question;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.buaa.project.dao.entity.QuestionDO;

/**
 * 题目分页查询请求参数
 */
@Data
public class QuestionPageReqDTO extends Page<QuestionDO> {

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 搜索词
     */
    private String search;

    /**
     * 是否解决 0：未解决 1：已解决 2：全部
     */
    private Integer solvedFlag;
}
