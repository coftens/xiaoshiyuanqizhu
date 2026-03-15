package com.xsyq.dto;

import lombok.Data;

@Data
public class MealFeedbackDTO {
    /**
     * 哪一餐: BREAKFAST, LUNCH, DINNER
     */
    private String mealType;
    
    /**
     * 反馈类型: AS_SUGGESTED (吃得刚好), OVERATE (吃多了), UNDERATE (没吃饱)
     */
    private String feedbackStatus;
}
