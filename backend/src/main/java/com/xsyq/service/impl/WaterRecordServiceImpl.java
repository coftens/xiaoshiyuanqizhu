package com.xsyq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xsyq.entity.WaterRecord;
import com.xsyq.mapper.WaterRecordMapper;
import com.xsyq.service.WaterRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WaterRecordServiceImpl implements WaterRecordService {

    private final WaterRecordMapper waterRecordMapper;

    @Override
    public void addWater(Long userId, Integer amountMl) {
        LocalDate today = LocalDate.now();
        
        LambdaQueryWrapper<WaterRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaterRecord::getUserId, userId)
               .eq(WaterRecord::getRecordDate, today);
               
        WaterRecord existRecord = waterRecordMapper.selectOne(wrapper);
        
        if (existRecord != null) {
            // 累加饮水量
            existRecord.setAmountMl(existRecord.getAmountMl() + amountMl);
            existRecord.setUpdatedAt(LocalDateTime.now());
            waterRecordMapper.updateById(existRecord);
        } else {
            // 新增记录
            WaterRecord newRecord = new WaterRecord();
            newRecord.setUserId(userId);
            newRecord.setRecordDate(today);
            newRecord.setAmountMl(amountMl);
            newRecord.setCreatedAt(LocalDateTime.now());
            newRecord.setUpdatedAt(LocalDateTime.now());
            waterRecordMapper.insert(newRecord);
        }
    }

    @Override
    public WaterRecord getTodayRecord(Long userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<WaterRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaterRecord::getUserId, userId)
               .eq(WaterRecord::getRecordDate, today);
        return waterRecordMapper.selectOne(wrapper);
    }
}
