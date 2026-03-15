package com.xsyq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("water_record")
public class WaterRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate recordDate;

    /**
     * 饮水总量，单位ml
     */
    private Integer amountMl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
