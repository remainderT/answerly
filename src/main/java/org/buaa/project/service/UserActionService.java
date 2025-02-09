package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buaa.project.common.enums.EntityTypeEnum;
import org.buaa.project.common.enums.UserActionTypeEnum;
import org.buaa.project.dao.entity.UserActionDO;

public interface UserActionService extends IService<UserActionDO> {

    /**
     * 检查用户行为是否存在
     */
    UserActionDO getUserAction(Long userId, EntityTypeEnum entityType, Long entityId);

    /**
     * 用户行为： 点赞、收藏、评论 等
     */
    void userAction(Long userId, EntityTypeEnum entityType, Long entityId, Long entityUserId, UserActionTypeEnum actionType);

    /**
     * 更新最后一次浏览时间
     */
    void updateLastViewTime(Long userId, EntityTypeEnum entityType, Long entityId);

}
