package com.xsyq.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xsyq.common.Result;
import com.xsyq.dto.AiRecommendResDTO;
import com.xsyq.dto.DailyNutritionDTO;
import com.xsyq.entity.UserProfile;
import com.xsyq.mapper.UserProfileMapper;
import com.xsyq.security.CustomUserDetails;
import com.xsyq.service.AiRecommendService;
import com.xsyq.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/meal")
public class AiRecommendController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private AiRecommendService aiRecommendService;

    // 获取AI生成的【今日三餐推荐】 + 【AI气泡语】
    @GetMapping("/recommend/ai")
    public Result<AiRecommendResDTO> getAiRecommend(
            @RequestParam(required = false) String dateStr,
            Authentication authentication) {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LocalDate targetDate = dateStr == null ? LocalDate.now() : LocalDate.parse(dateStr);

        // 1. 获取用户的身体档案
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);
        if (profile == null) {
            return Result.error(400, "请先完善身体档案");
        }

        // 2. 调用核心推演引擎算出今日数值
        DailyNutritionDTO nutritionTarget = scheduleService.calculateDailyNutrition(userId, targetDate);

        // 3. 将结果喂给阿里云千问生成自然语言的三餐指南
        AiRecommendResDTO aiRecommend = aiRecommendService.generateDailyRecommendation(profile, nutritionTarget);

        return Result.success(aiRecommend);
    }
}
