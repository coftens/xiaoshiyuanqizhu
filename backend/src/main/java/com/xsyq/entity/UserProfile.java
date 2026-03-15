package com.xsyq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfile {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer gender; // 1:男 2:女

    private BigDecimal heightCm;

    private BigDecimal weightKg;

    private Integer age;

    private BigDecimal bmi;

    private String goal; // LOSE_FAT / GAIN_MUSCLE / MAINTAIN / GUT_CARE / IMMUNITY

    private String allergy; // JSON数组字符串: ["milk","egg"]

    private String disease; // JSON数组字符串: ["gastritis","gout"]

    private String dietPreference; // JSON数组字符串: ["halal","vegetarian"]

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
