package com.xsyq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("meal_record")
public class MealRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate recordDate;

    /**
     * BREAKFAST, LUNCH, DINNER
     */
    private String mealType;

    /**
     * AS_SUGGESTED, OVERATE, UNDERATE
     */
    private String feedbackStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
