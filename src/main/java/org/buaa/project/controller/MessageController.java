package org.buaa.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.convention.result.Result;
import org.buaa.project.common.convention.result.Results;
import org.buaa.project.dto.req.message.MessageListPageReqDTO;
import org.buaa.project.dto.resp.MessagePageRespDTO;
import org.buaa.project.dto.resp.MessageSummaryRespDTO;
import org.buaa.project.service.MessageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息控制层
 */
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取所有类别的消息的大致信息
     */
    @GetMapping("/api/answerly/v1/message/summary")
    public Result<MessageSummaryRespDTO> getMessageUnread() {
        return Results.success(messageService.getMessageSummary());
    }

    /**
     * 根据类别分页查询消息
     */
    @GetMapping("/api/answerly/v1/message/page")
    public Result<IPage<MessagePageRespDTO>> pageMessage(MessageListPageReqDTO requestParam) {
        return Results.success(messageService.pageMessage(requestParam));
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/api/answerly/v1/message/delete")
    public Result<Void> deleteMessage(@RequestParam Long id) {
        messageService.deleteMessage(id);
        return Results.success();
    }


}
