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
     * 问题id
     */
    private Long questionId;

    /**
     * 是否收藏
     */
    private int collectStat;

    /**
     * 是否点赞
     */
    private int likeStat;

    /**
     * 是否评论
     */
    private int commentStat;

    /**
     * 上次浏览时间
     */
    private Date lastViewTime;

}
