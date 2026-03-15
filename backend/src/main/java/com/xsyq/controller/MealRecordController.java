package com.xsyq.controller;

import com.xsyq.common.Result;
import com.xsyq.dto.MealFeedbackDTO;
import com.xsyq.entity.MealRecord;
import com.xsyq.service.MealRecordService;
import com.xsyq.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
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
    public Result<Void> submitFeedback(@RequestBody MealFeedbackDTO feedbackDTO) {
        Long userId = SecurityUtils.getUserId();
        mealRecordService.submitFeedback(userId, feedbackDTO);
        return Result.success(null, "打卡反馈成功");
    }

    /**
     * 获取今天三餐的打卡状态
     */
    @GetMapping("/today")
    public Result<List<MealRecord>> getTodayRecords() {
        Long userId = SecurityUtils.getUserId();
        List<MealRecord> records = mealRecordService.getTodayRecords(userId);
        return Result.success(records);
    }
}
