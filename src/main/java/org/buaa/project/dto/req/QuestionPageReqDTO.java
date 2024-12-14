package org.buaa.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.buaa.project.dao.entity.QuestionDO;

/**
 * 问题分页查询请求
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
