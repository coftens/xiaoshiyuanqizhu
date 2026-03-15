package com.xsyq.service;

import com.xsyq.dto.AiRecommendResDTO;
import com.xsyq.dto.DailyNutritionDTO;
import com.xsyq.entity.UserProfile;

public interface AiRecommendService {
    /**
     * 根据身体档案和今日营养目标，调用阿里云百炼 Qwen 提供三餐推荐
     */
    AiRecommendResDTO generateDailyRecommendation(UserProfile profile, DailyNutritionDTO nutritionDTO);
}
