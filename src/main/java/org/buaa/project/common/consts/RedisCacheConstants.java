package org.buaa.project.common.consts;

/**
 * Redis缓存常量
 */
public class RedisCacheConstants {

    /**
     * 用户注册分布式锁
     */
    public static final String USER_REGISTER_LOCK_KEY = "answerly:user:register:lock:";

    /**
     * 用户注册验证码缓存
     */
    public static final String USER_REGISTER_CODE_KEY = "answerly:user:register:code:";

    /**
     * 用户注册验证码缓存过期时间
     */
    public static final long USER_REGISTER_CODE_EXPIRE_KEY = 5L;

    /**
     * 用户登录缓存标识
     */
    public static final String USER_LOGIN_KEY = "answerly:user:login:";

    /**
     * 用户登录缓存过期时间(天)
     */
    public static final long USER_LOGIN_EXPIRE_KEY = 30L;

    /**
     * 用户个人信息缓存标识
     */
    public static final String USER_INFO_KEY = "answerly:user:info:";

    /**
     * 题目统计信息缓存标识
     */
    public static final String QUESTION_COUNT_KEY = "answerly:question:count:";

    /**
     * 回答统计信息缓存标识
     */
    public static final String COMMENT_COUNT_KEY = "answerly:comment:count:";

    /**
     * 用户统计信息缓存标识
     */
    public static final String USER_COUNT_KEY = "answerly:user:count:";

    /**
     * 消息发送流缓存标识
     */
    public static final String MESSAGE_SEND_STREAM_KEY = "answerly:stream:message-send";

    /**
     * 数据同步流缓存标识
     */
    public static final String DATA_SYNC_STREAM_KEY = "answerly:stream:data-sync";

    /**
     * 用户登录图片验证码
     */
    public static final String USER_LOGIN_CAPTCHA_KEY = "answerly:user:login:captcha:";

    /**
     * 热门问题
     */
    public static final String HOT_QUESTION_KEY = "answerly:question:hot:";

    /**
     * 用户活跃度
     */
    public static final String ACTIVITY_SCORE_KEY = "answerly:user:activity:score:";

    /**
     * 问题内容缓存
     */
    public static final String QUESTION_CONTENT_KEY = "answerly:question:content:";

    /**
     * 问题分布式锁
     */
    public static final String QUESTION_LOCK_KEY = "answerly:question:lock:";

    /**
     * 分类内容缓存
     */
    public static final String CATEGORY_CONTENT_KEY = "answerly:category:content:";

    /**
     * 分类分布式锁
     */
    public static final String CATEGORY_LOCK_KEY = "answerly:category:lock:";

    /**
     * 总pv
     */
    public static final String TOTAL_PV_KEY = "answerly:system:pv:total";

    /**
     * 今日pv
     */
    public static final String TODAY_PV_KEY = "answerly:system:pv:today:";

    /**
     * 总uv
     */
    public static final String TOTAL_UV_KEY = "answerly:system:uv:total";

    /**
     * 今日uv
     */
    public static final String TODAY_UV_KEY = "answerly:system:uv:today:";

    /**
     * 签到
     */
    public static final String USER_SIGN = "answerly:user:sign";

}
