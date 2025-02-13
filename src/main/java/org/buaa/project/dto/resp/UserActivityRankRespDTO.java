package org.buaa.project.dto.resp;

import lombok.Data;

/**
 * 用户活跃度排行榜响应
 */
@Data
public class UserActivityRankRespDTO {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户活跃度
     */
    private Integer activity;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户简介
     */
    private String introduction;
}
