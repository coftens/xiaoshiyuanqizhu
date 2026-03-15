package com.xsyq.controller;

import com.xsyq.common.Result;
import com.xsyq.dto.UserRegisterDTO;
import com.xsyq.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/user")
public class UserAuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDTO dto) {
        String token = userService.register(dto);
        return Result.success(token);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody UserRegisterDTO dto) {
        String token = userService.login(dto);
        return Result.success(token);
    }
}
