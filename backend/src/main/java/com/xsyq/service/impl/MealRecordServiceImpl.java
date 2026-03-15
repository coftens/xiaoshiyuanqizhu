package com.xsyq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xsyq.dto.MealFeedbackDTO;
import com.xsyq.entity.MealRecord;
import com.xsyq.mapper.MealRecordMapper;
import com.xsyq.service.MealRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealRecordServiceImpl implements MealRecordService {

    private final MealRecordMapper mealRecordMapper;

    @Override
    public void submitFeedback(Long userId, MealFeedbackDTO feedbackDTO) {
        LocalDate today = LocalDate.now();
        
        // 查询今天是否已经打卡过当前餐次
        LambdaQueryWrapper<MealRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MealRecord::getUserId, userId)
               .eq(MealRecord::getRecordDate, today)
               .eq(MealRecord::getMealType, feedbackDTO.getMealType());
               
        MealRecord existRecord = mealRecordMapper.selectOne(wrapper);
        
        if (existRecord != null) {
            // 更新操作
            existRecord.setFeedbackStatus(feedbackDTO.getFeedbackStatus());
            existRecord.setUpdatedAt(LocalDateTime.now());
            mealRecordMapper.updateById(existRecord);
        } else {
            // 新增打卡
            MealRecord newRecord = new MealRecord();
            newRecord.setUserId(userId);
            newRecord.setRecordDate(today);
            newRecord.setMealType(feedbackDTO.getMealType());
            newRecord.setFeedbackStatus(feedbackDTO.getFeedbackStatus());
            newRecord.setCreatedAt(LocalDateTime.now());
            newRecord.setUpdatedAt(LocalDateTime.now());
            mealRecordMapper.insert(newRecord);
        }
    }

    @Override
    public List<MealRecord> getTodayRecords(Long userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<MealRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MealRecord::getUserId, userId)
               .eq(MealRecord::getRecordDate, today);
        return mealRecordMapper.selectList(wrapper);
    }
}
