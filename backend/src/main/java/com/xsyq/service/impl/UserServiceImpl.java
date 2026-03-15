package com.xsyq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xsyq.entity.User;
import com.xsyq.entity.UserProfile;
import com.xsyq.dto.UserRegisterDTO;
import com.xsyq.dto.UserProfileDTO;
import com.xsyq.mapper.UserMapper;
import com.xsyq.mapper.UserProfileMapper;
import com.xsyq.service.UserService;
import com.xsyq.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public String register(UserRegisterDTO dto) {
        // Check if user exists
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        if (this.baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该手机号已注册");
        }

        User user = new User();
        user.setPhone(dto.getPhone());
        // User表没有password字段（因为C端验证码登录设计），所以无需set password
        user.setCreatedAt(LocalDateTime.now());
        
        this.baseMapper.insert(user);

        // Auto login on register for convenience
        return jwtUtils.generateUserToken(user.getId());
    }

    @Override
    public String login(UserRegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = this.baseMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("该手机号尚未注册");
        }
        
        // 测试截获：统一模拟验证码为 123
        if (dto.getPassword() != null && !dto.getPassword().equals("123")) {
            throw new RuntimeException("验证码(模拟密码)错误，请使用 123");
        }

        return jwtUtils.generateUserToken(user.getId());
    }

    @Override
    @Transactional
    public void saveOrUpdateProfile(Long userId, UserProfileDTO dto) {
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);

        boolean isNew = (profile == null);
        if (isNew) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setCreatedAt(LocalDateTime.now());
        } else {
            profile.setUpdatedAt(LocalDateTime.now());
        }

        profile.setGender(dto.getGender());
        profile.setHeightCm(dto.getHeightCm());
        profile.setWeightKg(dto.getWeightKg());
        profile.setAge(dto.getAge());
        profile.setGoal(dto.getGoal());
        
        try {
            if(dto.getAllergy() != null) profile.setAllergy(objectMapper.writeValueAsString(dto.getAllergy()));
            if(dto.getDisease() != null) profile.setDisease(objectMapper.writeValueAsString(dto.getDisease()));
            if(dto.getDietPreference() != null) profile.setDietPreference(objectMapper.writeValueAsString(dto.getDietPreference()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("选项序列化失败");
        }

        // Calculate BMI: weight / (height * height) in meters
        if (dto.getWeightKg() != null && dto.getHeightCm() != null && dto.getHeightCm().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightM = dto.getHeightCm().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal bmi = dto.getWeightKg().divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP);
            profile.setBmi(bmi);
        }

        if (isNew) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }
    }
}
