package org.buaa.project.dto.resp;

import lombok.Data;

import java.util.Date;

/**
 * 消息分页返回参数
 */
@Data
public class MessagePageRespDTO {

    /**
     * 消息id
     */
    private Long id;

    /**
     * 消息发送者id
     */
    private Long fromId;

    /**
     * 消息接收者id
     */
    private Long toId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息状态
     */
    private Integer status;

    /**
     * 消息创建时间
     */
    private Date createTime;

}
