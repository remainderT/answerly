package org.buaa.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.buaa.project.dao.entity.UserDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
/**
 * 用户持久层
 */
public interface UserMapper extends BaseMapper<UserDO> {

}
