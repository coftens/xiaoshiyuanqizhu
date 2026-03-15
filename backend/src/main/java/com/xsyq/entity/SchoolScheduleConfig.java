package com.xsyq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalTime;

@Data
@TableName("school_schedule_config")
public class SchoolScheduleConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalTime morningStart;
    private LocalTime morningEnd;
    private Integer morningPeriods;

    private LocalTime afternoonStart;
    private LocalTime afternoonEnd;
    private Integer afternoonPeriods;

    private Boolean eveningEnabled;
    private LocalTime eveningStart;
    private LocalTime eveningEnd;
    private Integer eveningPeriods;
}
