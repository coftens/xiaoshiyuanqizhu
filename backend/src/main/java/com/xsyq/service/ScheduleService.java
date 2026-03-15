package com.xsyq.service;

import com.xsyq.dto.DailyNutritionDTO;
import com.xsyq.dto.ScheduleConfigDTO;
import com.xsyq.dto.ScheduleItemDTO;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    void saveScheduleConfig(Long userId, ScheduleConfigDTO dto);
    
    // 保存用户的某周矩阵课表
    void saveScheduleMatrix(Long userId, LocalDate weekStart, List<ScheduleItemDTO> items);
    
    // 计算当天的营养目标
    DailyNutritionDTO calculateDailyNutrition(Long userId, LocalDate date);
}
