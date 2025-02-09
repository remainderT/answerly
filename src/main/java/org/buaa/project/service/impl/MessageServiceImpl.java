package org.buaa.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.buaa.project.common.biz.user.UserContext;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dao.mapper.MessageMapper;
import org.buaa.project.dto.req.message.MessageListPageReqDTO;
import org.buaa.project.dto.resp.MessagePageRespDTO;
import org.buaa.project.dto.resp.MessageSummaryRespDTO;
import org.buaa.project.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息接口实现层
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl  extends ServiceImpl<MessageMapper, MessageDO> implements MessageService {

    @Override
    public MessageSummaryRespDTO getMessageSummary() {
        MessageSummaryRespDTO messageUnreadRespDTO = new MessageSummaryRespDTO();

        String[] messageTypes = {"system", "like", "comment", "collect", "useful"};
        List<HashMap<String, Object>> messageSummaryList = new ArrayList<>();

        for (String messageType : messageTypes) {
            LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                    .eq(MessageDO::getToId, UserContext.getUserId())
                    .eq(MessageDO::getDelFlag, 0)
                    .eq(MessageDO::getType, messageType);

            List<MessageDO> allMessages = baseMapper.selectList(queryWrapper);
            int totalCount = allMessages.size();

            List<MessageDO> unreadMessages = allMessages.stream()
                    .filter(message -> message.getStatus() == 0)
                    .sorted(Comparator.comparing(MessageDO::getCreateTime))
                    .toList();

            int unreadCount = unreadMessages.size();

            MessageDO firstMessage = allMessages.isEmpty() ? null : allMessages.get(0);

            HashMap<String, Object> messageSummary = new HashMap<>();
            messageSummary.put("type", messageType);
            messageSummary.put("totalCount", totalCount);
            messageSummary.put("unreadCount", unreadCount);
            messageSummary.put("firstUnreadMessage", firstMessage);
            messageSummaryList.add(messageSummary);
        }

        messageUnreadRespDTO.setMessageSummary(messageSummaryList);

        return messageUnreadRespDTO;
    }

    @Override
    public IPage<MessagePageRespDTO> pageMessage(MessageListPageReqDTO requestParam) {
        LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getToId, UserContext.getUserId())
                .eq(MessageDO::getType, requestParam.getType())
                .eq(MessageDO::getDelFlag, 0)
                .orderByDesc(MessageDO::getCreateTime);
        IPage<MessageDO> page = baseMapper.selectPage(requestParam, queryWrapper);

        List<MessageDO> messageList = page.getRecords();
        if (!messageList.isEmpty()) {
            LambdaUpdateWrapper<MessageDO> updateWrapper = Wrappers.lambdaUpdate(MessageDO.class)
                    .in(MessageDO::getId, messageList.stream().map(MessageDO::getId).collect(Collectors.toList()))
                    .eq(MessageDO::getStatus, 0)
                    .set(MessageDO::getStatus, 1);

            baseMapper.update(null, updateWrapper);
        }
        return page.convert(each -> BeanUtil.toBean(each, MessagePageRespDTO.class));
    }

    @Override
    public void deleteMessage(Long id) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<MessageDO> queryWrapper = Wrappers.lambdaQuery(MessageDO.class)
                .eq(MessageDO::getId, id)
                .eq(MessageDO::getToId, userId)
                .eq(MessageDO::getDelFlag, 0);
        MessageDO messageDO = baseMapper.selectOne(queryWrapper);
        if (messageDO != null) {
            messageDO.setDelFlag(1);
            baseMapper.updateById(messageDO);
        }
    }
}
