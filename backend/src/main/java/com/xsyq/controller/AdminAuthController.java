package com.xsyq.controller;

import com.xsyq.common.Result;
import com.xsyq.dto.AdminLoginDTO;
import com.xsyq.service.SysAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final SysAdminService sysAdminService;

    /**
     * 后台管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        try {
            Map<String, Object> tokenInfo = sysAdminService.login(loginDTO);
            return Result.success(tokenInfo);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 测试验证权限接口
     */
    @GetMapping("/test")
    public Result<String> testAdminAuth() {
        return Result.success("管理员权限验证通过！如果你看到了这句话，说明 Token 和 Security 没问题。");
    }
}
