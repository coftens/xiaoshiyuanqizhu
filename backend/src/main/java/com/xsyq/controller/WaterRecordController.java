package com.xsyq.controller;

import com.xsyq.common.Result;
import com.xsyq.dto.WaterRecordDTO;
import com.xsyq.entity.WaterRecord;
import com.xsyq.security.CustomUserDetails;
import com.xsyq.service.WaterRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/water")
@RequiredArgsConstructor
public class WaterRecordController {

    private final WaterRecordService waterRecordService;

    /**
     * 打卡饮水 (每次喝水调用，如传 +250)
     */
    @PostMapping("/add")
    public Result<Void> addWater(@RequestBody WaterRecordDTO dto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        waterRecordService.addWater(userId, dto.getAmountMl());
        return Result.success();
    }

    /**
     * 获取今天饮水情况
     */
    @GetMapping("/today")
    public Result<WaterRecord> getTodayWater(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        WaterRecord record = waterRecordService.getTodayRecord(userId);
        if (record == null) {
            record = new WaterRecord();
            record.setAmountMl(0); // 没有数据默认为0
        }
        return Result.success(record);
    }
}
