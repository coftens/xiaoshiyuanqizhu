package com.xsyq.controller;

import com.xsyq.common.Result;
import com.xsyq.dto.DailyNutritionDTO;
import com.xsyq.dto.ScheduleConfigDTO;
import com.xsyq.dto.ScheduleMatrixRequestDTO;
import com.xsyq.security.CustomUserDetails;
import com.xsyq.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    // 1. 设置用户的作息维度 (上课时间、节数)
    @PostMapping("/config")
    public Result<Void> setScheduleConfig(@RequestBody ScheduleConfigDTO dto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        scheduleService.saveScheduleConfig(userDetails.getId(), dto);
        return Result.success(null);
    }

    // 2. 批量上传/保存某周的核心排课矩阵 (哪些格子是体育课、文化课、理科课)
    @PostMapping("/matrix")
    public Result<Void> saveScheduleMatrix(@RequestBody ScheduleMatrixRequestDTO dto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        scheduleService.saveScheduleMatrix(userDetails.getId(), dto.getWeekStartDate(), dto.getItems());
        return Result.success(null);
    }

    // 3. ✨ 核心引擎出口：获取当天的动态热量和核心营养推荐拆分
    @GetMapping("/nutrition/today")
    public Result<DailyNutritionDTO> getTodayNutritionConfig(
            @RequestParam(required = false) String dateStr,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        LocalDate targetDate = dateStr == null ? LocalDate.now() : LocalDate.parse(dateStr);
        DailyNutritionDTO nutritionTarget = scheduleService.calculateDailyNutrition(userDetails.getId(), targetDate);
        
        return Result.success(nutritionTarget);
    }
}
