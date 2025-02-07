package org.buaa.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.buaa.project.dao.entity.AnswerDO;

/**
 * 回答分页查询请求
 */
@Data
public class AnswerPageReqDTP extends Page<AnswerDO> {

    /**
     * 问题id
     */
    private Long id;

}
