package com.xsyq.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiRecommendResDTO {
    private String dailyComment; // AI给用户的贴心气泡寄语
    private List<MealRecommendDTO> meals; // 三餐推荐
}
