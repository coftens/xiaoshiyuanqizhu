package com.xsyq.controller;

import com.xsyq.common.Result;
import com.xsyq.dto.WeeklyReportDTO;
import com.xsyq.security.CustomUserDetails;
import com.xsyq.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 获取用户本周的健康饮食打卡周报
     */
    @GetMapping("/weekly")
    public Result<WeeklyReportDTO> getWeeklyReport(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        WeeklyReportDTO report = reportService.generateWeeklyReport(userId);
        return Result.success(report);
    }

}
