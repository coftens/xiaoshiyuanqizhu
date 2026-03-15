package com.xsyq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xsyq.entity.User;
import com.xsyq.dto.UserRegisterDTO;
import com.xsyq.dto.UserProfileDTO;

public interface UserService extends IService<User> {
    String register(UserRegisterDTO dto);
    String login(UserRegisterDTO dto);
    void saveOrUpdateProfile(Long userId, UserProfileDTO dto);
}
