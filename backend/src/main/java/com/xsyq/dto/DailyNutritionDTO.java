package com.xsyq.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DailyNutritionDTO {
    private Integer bmr;       // 基础代谢
    private BigDecimal pal;    // 活动系数
    private Integer tdee;      // 每日总消耗
    private Integer targetCal; // 目标摄入热量
    
    // 三大营养素目标
    private Integer proteinG;
    private Integer carbG;
    private Integer fatG;
    
    // 每日饮水推荐
    private Integer waterMl;
    
    // 基础三餐热量拆分分配 (直接算好的展示用)
    private Integer breakfastCal;
    private Integer lunchCal;
    private Integer dinnerCal;
}
