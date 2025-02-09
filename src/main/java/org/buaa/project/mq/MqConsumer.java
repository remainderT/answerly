package org.buaa.project.mq;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buaa.project.common.convention.exception.ServiceException;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.MessageTypeEnum;
import org.buaa.project.dao.entity.CommentDO;
import org.buaa.project.dao.entity.MessageDO;
import org.buaa.project.dao.entity.QuestionDO;
import org.buaa.project.dao.entity.UserActionDO;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dao.mapper.CommentMapper;
import org.buaa.project.dao.mapper.MessageMapper;
import org.buaa.project.dao.mapper.QuestionMapper;
import org.buaa.project.dao.mapper.UserActionMapper;
import org.buaa.project.dao.mapper.UserMapper;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final StringRedisTemplate stringRedisTemplate;

    private final MqIdempotent mqIdempotent;

    private final MessageMapper messageMapper;

    private final UserMapper userMapper;

    private final QuestionMapper questionMapper;

    private final CommentMapper commentMapper;

    private final UserActionMapper userActionMapper;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String stream = message.getStream();
        RecordId id = message.getId();
        if (mqIdempotent.isMessageBeingConsumed(id.toString())) {
            // 判断当前的这个消息流程是否执行完成
            if (mqIdempotent.isAccomplish(id.toString())) {
                return;
            }
            throw new ServiceException("消息未完成流程，需要消息队列重试");
        }
        try {
            Map<String, String> producerMap = message.getValue();
            MqEvent event = JSON.parseObject(producerMap.get("event"), MqEvent.class);
            consume(event);
            stringRedisTemplate.opsForStream().delete(Objects.requireNonNull(stream), id.getValue());
        } catch (Throwable ex) {
            // 某某某情况宕机了
            mqIdempotent.delMessageProcessed(id.toString());
            log.error("消费异常", ex);
            throw ex;
        }
        mqIdempotent.setAccomplish(id.toString());
    }

    public void consume(MqEvent event) {
        if (!Objects.equals(event.getUserId(), event.getEntityUserId())) {
            UserDO from = userMapper.selectById(event.getUserId());
            CommentDO comment = null;
            QuestionDO question = null;
            String content = null;
            UserDO to = userMapper.selectById(event.getEntityUserId());
            MessageTypeEnum messageType = event.getMessageType();
            Boolean isPositive = (Boolean) event.getData().get("isPositive");
            // 序列化问题
            UserActionDO userActionDO = userActionMapper.selectById(event.getData().get("userActionId").toString());
            if (!Objects.equals(isPositive, false)) {
                switch (messageType) {
                    case LIKE:
                        if (event.getEntityType().equals(EntityTypeEnum.COMMENT)) {
                            comment = commentMapper.selectById(event.getEntityId());
                            commentMapper.updateById(comment);
                            question = questionMapper.selectById(comment.getQuestionId());
                            content = "(%s)点赞了你在(%s)问题下的(%s)评论".formatted(from.getUsername(), question.getTitle(), comment.getContent());
                        } else if (event.getEntityType().equals(EntityTypeEnum.QUESTION)) {
                            question = questionMapper.selectById(event.getEntityId());
                            questionMapper.updateById(question);
                            content = "(%s)点赞了你的(%s)问题".formatted(from.getUsername(), question.getTitle());
                        }
                        to.setLikeCount(to.getLikeCount() + 1);
                        userMapper.updateById(to);
                        break;

                    case COMMENT:
                        if (event.getEntityType().equals(EntityTypeEnum.COMMENT)) {
                            comment = commentMapper.selectById(event.getEntityId());
                            question = questionMapper.selectById(comment.getQuestionId());
                            content = "(%s)回复了你在(%s)问题下的(%s)评论".formatted(from.getUsername(), question.getTitle(), comment.getContent());
                        } else if (event.getEntityType().equals(EntityTypeEnum.QUESTION)) {
                            question = questionMapper.selectById(event.getEntityId());
                            content = "(%s)评论了你的(%s)问题".formatted(from.getUsername(), question.getTitle());
                        }
                        break;

                    case COLLECT:
                        question = questionMapper.selectById(event.getEntityId());
                        content = "(%s)收藏了你的(%s)问题".formatted(from.getUsername(), question.getTitle());
                        break;

                    case USEFUL:
                        comment = commentMapper.selectById(event.getEntityId());
                        question = questionMapper.selectById(event.getEntityId());
                        content = "(%s)认为你在(%s)问题下的(%s)评论有用".formatted(from.getUsername(), question.getTitle(), comment.getContent());
                        break;

                    case SYSTEM:
                        content = event.getData().get("content").toString();
                        break;

                    default:
                        break;
                }
                MessageDO messageDO = MessageDO.builder()
                        .fromId(event.getUserId())
                        .toId(event.getEntityUserId())
                        .type(event.getMessageType().toString())
                        .content(content)
                        .build();
                messageMapper.insert(messageDO);

                userActionDO.setMessageId(messageDO.getId());
                userActionMapper.updateById(userActionDO);
            } else {
                // 删除消息
                MessageDO messageDO = messageMapper.selectById(userActionDO.getMessageId());
                messageDO.setDelFlag(1);
                messageMapper.updateById(messageDO);
            }
        }
    }

}
