package com.xsyq.controller;

import com.xsyq.common.Result;
import com.xsyq.dto.UserProfileDTO;
import com.xsyq.security.CustomUserDetails;
import com.xsyq.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @PostMapping("/setup")
    public Result<Void> setupProfile(@RequestBody UserProfileDTO dto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        userService.saveOrUpdateProfile(userId, dto);
        return Result.success(null);
    }
}
