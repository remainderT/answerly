package org.buaa.project.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.buaa.project.toolkit.serialize.PhoneDesensitizationSerializer;

/**
 * 用户返回参数响应
 */
@Data
public class UserRespDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 个人简介
     */
    private String introduction;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 解决问题的数量
     */
    private Integer solvedCount;

    /**
     * 手机号
     */
    @JsonSerialize(using = PhoneDesensitizationSerializer.class)
    private String phone;

}
