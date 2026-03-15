package com.xsyq.controller;

import com.xsyq.common.Result;
import com.xsyq.dto.MealFeedbackDTO;
import com.xsyq.entity.MealRecord;
import com.xsyq.security.CustomUserDetails;
import com.xsyq.service.MealRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal/record")
@RequiredArgsConstructor
public class MealRecordController {

    private final MealRecordService mealRecordService;

    /**
     * 提交餐后反馈(按建议吃、吃多了、没吃饱)
     */
    @PostMapping("/feedback")
    public Result<Void> submitFeedback(@RequestBody MealFeedbackDTO feedbackDTO, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        mealRecordService.submitFeedback(userId, feedbackDTO);
        return Result.success();
    }

    /**
     * 获取今天三餐的打卡状态
     */
    @GetMapping("/today")
    public Result<List<MealRecord>> getTodayRecords(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        List<MealRecord> records = mealRecordService.getTodayRecords(userId);
        return Result.success(records);
    }
}
