package org.buaa.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.buaa.project.dao.entity.UserDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
/**
 * 用户持久层
 */
public interface UserMapper extends BaseMapper<UserDO> {

    // 根据邮箱查询用户名
    @Select("SELECT username FROM user WHERE mail = #{mail} AND del_flag = 0")
    String selectUsernameByMail(@Param("mail") String mail);

    // 根据用户名查询邮箱
    @Select("SELECT mail FROM user WHERE username = #{username} AND del_flag = 0")
    String selectMailByUsername(@Param("username") String username);
}
