package org.buaa.project.service;

import org.buaa.project.common.enums.EntityTypeEnum;

/**
 * 点赞接口层
 */
public interface LikeService {

    /**
     * 点赞
     */
    void like(Long userId, EntityTypeEnum entityType, Long entityId, Long entityUserId);

    /**
     * 查询实体点赞数量
     */
    int findEntityLikeCount(EntityTypeEnum entityType, Long entityId);

    /**
     * 查询实体点赞状态
     */
    String findEntityLikeStatus(Long userId, EntityTypeEnum entityType, Long entityId);

    /**
     * 查询用户点赞数量
     */
    int findUserLikeCount(Long userId);
}
