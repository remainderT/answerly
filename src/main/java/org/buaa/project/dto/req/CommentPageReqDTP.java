package org.buaa.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.buaa.project.dao.entity.CommentDO;

/**
 * 评论分页查询请求
 */
@Data
public class CommentPageReqDTP extends Page<CommentDO> {

    /**
     * 问题id
     */
    private Long id;

}
