package com.xsyq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate weekStartDate; // 本周一日期
    
    private Integer weekday; // 1-5 (周一到周五)
    
    private Integer period; // 第几节课
    
    private String courseType; // NONE / CULTURE / SPORTS / SCIENCE
    
    private String courseName;
}
