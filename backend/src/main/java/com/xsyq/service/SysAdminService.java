package com.xsyq.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xsyq.dto.AdminLoginDTO;
import com.xsyq.entity.SysAdmin;
import com.xsyq.mapper.SysAdminMapper;
import com.xsyq.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysAdminService {

    private final SysAdminMapper sysAdminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * 管理员登录
     */
    public Map<String, Object> login(AdminLoginDTO loginDTO) {
        SysAdmin admin = sysAdminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getUsername, loginDTO.getUsername())
        );

        if (admin == null || admin.getStatus() == 0) {
            throw new RuntimeException("账号不存在或已被禁用");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成 Token
        String token = jwtUtils.generateAdminToken(admin.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", admin);
        
        return result;
    }
}
