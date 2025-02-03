package org.buaa.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.ServletRequest;
import org.buaa.project.dao.entity.UserDO;
import org.buaa.project.dto.req.UserLoginReqDTO;
import org.buaa.project.dto.req.UserRegisterReqDTO;
import org.buaa.project.dto.req.UserUpdateReqDTO;
import org.buaa.project.dto.resp.UserLoginRespDTO;
import org.buaa.project.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     *
     * @param username
     * @return
     */
    Boolean hasUsername(String username);


    /**
     * 发送验证码
     */
    Boolean sendCode(String mail);


    /**
     * 注册用户
     *
     * @param requestParam 注册用户请求参数
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 用户登录
     *
     * @param requestParam 用户登录请求参数
     * @return 用户登录返回参数 Token
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam, ServletRequest request);

    /**
     * 检查用户是否登录
     *
     * @param username 用户名
     * @param token    用户登录 Token
     */
    Boolean checkLogin(String username, String token);

    /**
     * 退出登录
     *
     * @param username 用户名
     * @param token    用户登录 Token
     */
    void logout(String username, String token);

    /**
     * 更新用户信息
     *
     * @param requestParam 更新用户信息请求参数
     */
    void update(UserUpdateReqDTO requestParam);


}
