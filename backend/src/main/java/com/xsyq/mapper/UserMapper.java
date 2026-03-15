package com.xsyq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xsyq.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
