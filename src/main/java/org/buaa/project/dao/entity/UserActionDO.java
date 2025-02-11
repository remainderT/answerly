package org.buaa.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.buaa.project.common.database.BaseDO;

import java.util.Date;

/**
 * 用户行为持久层实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_action")
public class UserActionDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 实体类型
     */
    private String entityType;

    /**
     * 实体id
     */
    private Long entityId;

    /**
     * 是否收藏
     */
    private Integer collectStat;

    /**
     * 是否认为有用
     */
    private Integer usefulStat;

    /**
     * 是否点赞
     */
    private Integer likeStat;

    /**
     * 消息id
     */
    private Long messageId;

    /**
     * 上次浏览时间
     */
    private Date lastViewTime;

}
