DROP DATABASE IF EXISTS answerly;
CREATE DATABASE answerly  DEFAULT CHARACTER SET utf8mb4;

USE `answerly`;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                        `username`      varchar(256) NOT NULL COMMENT '用户名',
                        `password`      varchar(512) NOT NULL COMMENT '密码',
                        `mail`          varchar(30)  NOT NULL COMMENT '邮箱',
                        `salt`          varchar(20)  NOT NULL COMMENT '盐',
                        `avatar`        varchar(60)     DEFAULT NULL COMMENT '头像',
                        `phone`         varchar(20)     DEFAULT NULL COMMENT '手机号',
                        `introduction`  varchar(1024)   DEFAULT NULL COMMENT '个人简介',
                        `like_count`    int(11)         DEFAULT 0 COMMENT '点赞数',
                        `collect_count`  int(11)         DEFAULT 0 COMMENT '收藏题目的数量',
                        `useful_count` int(11)          DEFAULT 0 COMMENT '评论有用数量',
                        `user_type` ENUM('student', 'volunteer','admin') NOT NULL COMMENT '用户类型',
                        `status` tinyint(4)        DEFAULT 0    COMMENT '状态',
                        `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                        `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                        `del_flag`    tinyint(1)   DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY idx_unique_username (username) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生和义工和管理员';


INSERT INTO `user` (`username`, `password`, `salt`, `mail`, `avatar`, `phone`, `introduction`, `like_count`, `collect_count`, `useful_count`, `user_type`, `status`, `create_time`, `update_time`, `del_flag`)
VALUES ('admin', 'e62ee014c28a13e75d90df35d04f6faf', '13246','admin@example.com', NULL, NULL, 'Administrator account', 0, 0, 0, 'admin', 1, NOW(), NOW(), 0);


DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                            `name`  varchar(50)        NOT NULL COMMENT '分类名称',
                            `image` varchar(60)        DEFAULT NULL COMMENT '图片',
                            `sort` int(11)             DEFAULT 0    COMMENT '排序',
                            `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                            `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                            `del_flag`    tinyint(1)   DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类';
INSERT INTO `category` (`name`, `image`, `sort`, `create_time`, `update_time`, `del_flag`)
VALUES
    ('数分', '2025/03/24/7f455906-0e4a-4337-9c60-8ebab56c3d93.png', 1, NOW(), NOW(), 0),
    ('高代', '2025/03/24/737b4c5c-1667-4062-915d-c298c805077b.png', 2, NOW(), NOW(), 0),
    ('程设', '2025/03/24/a3ece88c-cbe9-4ee1-8b21-7d6e23c3e316.png', 3, NOW(), NOW(), 0),
    ('离散数学', '2025/03/24/abbc9f42-21fe-4ffd-8f98-b5c954ef8898.png', 4, NOW(), NOW(), 0),
    ('基础物理', '2025/03/24/c64c646f-a3e7-4669-813c-4f9ccc288556.png', 5, NOW(), NOW(), 0),
    ('数据结构', '2025/03/24/1a55f960-5a53-4539-882f-9fa91c793768.png', 6, NOW(), NOW(), 0);


DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                            `category_id` bigint(20)   NOT NULL COMMENT '分类ID',
                            `title` varchar(256)       NOT NULL COMMENT '标题',
                            `content` varchar(2048)    DEFAULT NULL COMMENT '内容',
                            `user_id` bigint(20)       NOT NULL COMMENT '发布人ID',
                            `username` varchar(256)    NOT NULL COMMENT '用户名',
                            `images` varchar(600)      DEFAULT NULL COMMENT '照片路径，最多10张，多张以","隔开',
                            `view_count` int(11)       DEFAULT 0 COMMENT '浏览量',
                            `like_count` int(11)       DEFAULT 0 COMMENT '点赞数',
                            `comment_count` int(11)    DEFAULT 0 COMMENT '评论数',
                            `solved_flag` tinyint(1)   DEFAULT 0 COMMENT '是否解决 0：未解决 1：已解决',
                            `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                            `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                            `del_flag`   tinyint(1)    DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                            PRIMARY KEY (`id`),
                            KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题';


DROP TABLE IF EXISTS `user_action`;
CREATE TABLE `user_action` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                               `user_id` bigint(20)  unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
                               `entity_type` ENUM('question', 'comment') NOT NULL  COMMENT '实体类型',
                               `entity_id` bigint(20)  unsigned NOT NULL DEFAULT '0' COMMENT '实体ID',
                               `collect_stat` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '收藏状态: 0-未收藏，1-已收藏',
                               `like_stat` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '点赞状态: 0-未点赞，1-点赞',
                               `useful_stat` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '评论有用状态: 0-未有用，1-有用',
                               `message_id` bigint(20)  unsigned NOT NULL DEFAULT '0' COMMENT '消息ID',
                               `last_view_time` datetime     DEFAULT NULL COMMENT '上次浏览时间',
                               `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                               `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                               `del_flag`   tinyint(1)    DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                               PRIMARY KEY (`id`),
                               KEY `idx_user_entity_type_id` (`user_id`, `entity_type`, `entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为';


DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                           `user_id` bigint(20)       NOT NULL COMMENT '发布人ID',
                           `username` varchar(256)    NOT NULL COMMENT '用户名',
                           `question_id` bigint(20)   NOT NULL COMMENT '问题ID',
                           `content` varchar(2048)    DEFAULT NULL COMMENT '内容',
                           `top_comment_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '顶级评论ID',
                           `parent_comment_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父评论ID',
                           `images` varchar(600)      DEFAULT NULL COMMENT '照片路径，最多10张，多张以","隔开',
                           `like_count` int(11)       DEFAULT 0 COMMENT '点赞数',
                           `useful` tinyint(1)        DEFAULT 0 COMMENT '是否有用',
                           `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                           `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                           `del_flag`    tinyint(1)   DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回复';

DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                           `from_id` bigint(20) NOT NULL COMMENT '发送人ID 1就是系统消息',
                           `to_id` bigint(20) NOT NULL COMMENT '接收者ID',
                           `type` ENUM('system', 'like', 'comment', 'collect', 'useful') NOT NULL COMMENT '消息类型',
                           `content` text   DEFAULT NULL COMMENT '内容',
                           `status` int(11) DEFAULT 0 COMMENT '0-未读;1-已读',
                           `generate_id` bigint(20) NOT NULL COMMENT '产生消息的ID 可能来自 comment 表 也可能来自 user_action 表',
                           `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                           `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                           `del_flag`    tinyint(1)   DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                           PRIMARY KEY (`id`),
                           KEY `index_to_id` (`to_id`),
                           KEY `index_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息';