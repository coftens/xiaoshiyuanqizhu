package com.xsyq.service;

import com.xsyq.entity.WaterRecord;

public interface WaterRecordService {

    /**
     * 增加饮水量 (允许增量添加，比如每次 +250ml)
     */
    void addWater(Long userId, Integer amountMl);

    /**
     * 获取今天总饮水量
     */
    WaterRecord getTodayRecord(Long userId);
}
