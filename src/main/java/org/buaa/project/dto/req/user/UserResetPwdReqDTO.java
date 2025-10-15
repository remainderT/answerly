package org.buaa.project.dto.req.user;

import lombok.Data;

/**
 * 用户重置密码请求参数
 */

@Data
public class UserResetPwdReqDTO {
    private String username;
    private String code;
    private String newPassword;
}