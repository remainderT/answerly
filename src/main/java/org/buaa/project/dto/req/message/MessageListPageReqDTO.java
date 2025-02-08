package org.buaa.project.dto.req.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.buaa.project.dao.entity.MessageDO;

/**
 * 根据类别分页查询消息请求参数
 */
@Data
public class MessageListPageReqDTO extends Page<MessageDO> {

    /**
     * 类别
     */
    private String type;
}
