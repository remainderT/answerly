package org.buaa.project.service;

import org.buaa.project.common.enums.EntityTypeEnum;

/**
 * 点赞接口层
 */
public interface LikeService {

    /**
     * 点赞
     * @param userId
     * @param entityType
     * @param entityId
     */
    void like(long userId, EntityTypeEnum entityType, long entityId, long entityUserId);

    /**
     * 查询实体点赞数量
     * @param entityType
     * @param entityId
     * @return
     */
    int findEntityLikeCount(EntityTypeEnum entityType, long entityId);

    /**
     * 查询实体点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    String findEntityLikeStatus(long userId, EntityTypeEnum entityType, long entityId);

    /**
     * 查询用户点赞数量
     * @param userId
     * @return
     */
    int findUserLikeCount(long userId);
}
