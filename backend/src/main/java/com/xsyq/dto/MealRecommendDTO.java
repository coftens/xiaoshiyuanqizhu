package com.xsyq.dto;

import lombok.Data;

@Data
public class MealRecommendDTO {
    private String mealName; // 早餐、午餐、晚餐
    private String recommendedFood; // 推荐食物名称或组合
    private Integer estimatedCalories; // 预估热量
    private String reason; // 推荐理由
}
