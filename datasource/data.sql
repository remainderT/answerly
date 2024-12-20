DROP DATABASE IF EXISTS answerly;
CREATE DATABASE answerly  DEFAULT CHARACTER SET utf8mb4;

USE `answerly`;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                        `username`      varchar(256) NOT NULL COMMENT '用户名',
                        `password`      varchar(512) NOT NULL COMMENT '密码',
                        `mail`          varchar(20)  NOT NULL COMMENT '邮箱',
                        `avatar`         varchar(60)     DEFAULT NULL COMMENT '头像',
                        `phone`         varchar(20)     DEFAULT NULL COMMENT '手机号',
                        `introduction`  varchar(1024)   DEFAULT NULL COMMENT '个人简介',
                        `like_count`    int(11)         DEFAULT 0 COMMENT '点赞数',
                        `solved_count`  int(11)         DEFAULT 0 COMMENT '解决问题的数量',
                        `user_type` ENUM('student', 'volunteer','admin') NOT NULL COMMENT '用户类型',
                        `status` tinyint(4)        DEFAULT 0    COMMENT '状态',
                        `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                        `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                        `del_flag`    tinyint(1)   DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY idx_unique_username (username) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生和义工和管理员';


INSERT INTO `user` (`username`, `password`, `mail`, `avatar`, `phone`, `introduction`, `like_count`, `solved_count`, `user_type`, `status`, `create_time`, `update_time`, `del_flag`)
VALUES ('admin', 'Vx7!nE9z$T4m@P2qW#jZ', 'admin@example.com', NULL, NULL, 'Administrator account', 0, 0, 'admin', 1, NOW(), NOW(), 0);



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
                            `solved_flag` tinyint(1)   DEFAULT 0 COMMENT '是否解决 0：未解决 1：已解决',
                            `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                            `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                            `del_flag`   tinyint(1)    DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                            PRIMARY KEY (`id`),
                            KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题';


DROP TABLE IF EXISTS `answer`;
CREATE TABLE `answer` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
                          `user_id` bigint(20)       NOT NULL COMMENT '发布人ID',
                          `username` varchar(256)    NOT NULL COMMENT '用户名',
                          `question_id` bigint(20)   NOT NULL COMMENT '问题ID',
                          `content` varchar(2048)    DEFAULT NULL COMMENT '内容',
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
                           `type` ENUM('system', 'like', 'answer') NOT NULL COMMENT '消息类型',
                           `content` text   DEFAULT NULL COMMENT '内容',
                           `status` int(11) DEFAULT 0 COMMENT '0-未读;1-已读;2-删除;',
                           `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
                           `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
                           `del_flag`    tinyint(1)   DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
                           PRIMARY KEY (`id`),
                           KEY `index_to_id` (`to_id`),
                           KEY `index_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息';