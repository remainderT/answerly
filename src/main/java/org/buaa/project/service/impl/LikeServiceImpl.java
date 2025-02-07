package org.buaa.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.MessageTypeEnum;
import org.buaa.project.mq.MqEvent;
import org.buaa.project.mq.MqProducer;
import org.buaa.project.service.LikeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static org.buaa.project.common.consts.RedisCacheConstants.PREFIX_ENTITY_LIKE;

/**
 * 点赞接口实现层
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final StringRedisTemplate stringRedisTemplate;

    private final MqProducer producer;

    @Override
    public void like(Long userId, EntityTypeEnum entityType, Long entityId, Long entityUserId) {
        String entityLikeKey = String.format(PREFIX_ENTITY_LIKE, entityType, entityId);
        String userLikeKey = String.format(PREFIX_ENTITY_LIKE, EntityTypeEnum.USER, entityUserId);
        boolean isMember = Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(entityLikeKey, String.valueOf(userId)));
        if (isMember) {
            stringRedisTemplate.opsForSet().remove(entityLikeKey, String.valueOf(userId));
            stringRedisTemplate.opsForValue().decrement(userLikeKey);
        } else {
            stringRedisTemplate.opsForSet().add(entityLikeKey, String.valueOf(userId));
            stringRedisTemplate.opsForValue().increment(userLikeKey);
            MqEvent event = MqEvent.builder()
                    .messageType(MessageTypeEnum.Like)
                    .entityType(entityType)
                    .userId(userId)
                    .entityId(entityId)
                    .entityUserId(entityUserId)
                    .build();
            producer.send(event);
        }
    }

    @Override
    public int findEntityLikeCount(EntityTypeEnum entityType, Long entityId) {
        String entityLikeKey = String.format(PREFIX_ENTITY_LIKE, entityType, entityId);
        Long size = stringRedisTemplate.opsForSet().size(entityLikeKey);
        return size != null ? size.intValue() : 0;
    }

    @Override
    public String findEntityLikeStatus(Long userId, EntityTypeEnum entityType, Long entityId) {
        String entityLikeKey = String.format(PREFIX_ENTITY_LIKE, entityType, entityId);
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(entityLikeKey, String.valueOf(userId))) ? "已点赞" : "未点赞";
    }

    @Override
    public int findUserLikeCount(Long userId) {
        String entityLikeKey = String.format(PREFIX_ENTITY_LIKE, EntityTypeEnum.USER, userId);
        String size = stringRedisTemplate.opsForValue().get(entityLikeKey);
        return size != null ? Integer.parseInt(size) : 0;
    }


}
