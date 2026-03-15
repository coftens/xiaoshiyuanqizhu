package com.xsyq.service;

import com.xsyq.dto.WeeklyReportDTO;

public interface ReportService {
    
    /**
     * 汇总前7天的用户打卡数据，并生成周报返回。
     * @param userId 用户ID
     * @return 包含各项指标与AI提示的周报对象
     */
    WeeklyReportDTO generateWeeklyReport(Long userId);

}
