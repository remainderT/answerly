package org.buaa.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dto.req.message.MessageListPageReqDTO;
import org.buaa.project.dto.resp.MessagePageRespDTO;
import org.buaa.project.dto.resp.MessageSummaryRespDTO;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 消息接口层
 */
public interface MessageService extends IService<MessageDO> {

    /**
     * 获取所有类别的消息的大致信息
     */
    MessageSummaryRespDTO getMessageSummary();

    /**
     * 根据类别分页查询消息
     */
    IPage<MessagePageRespDTO> pageMessage(MessageListPageReqDTO requestParam);

    /**
     * 删除消息
     */
    void deleteMessage(@RequestParam Long id);
}
