package com.xsyq.service;

import com.xsyq.dto.MealFeedbackDTO;
import com.xsyq.entity.MealRecord;
import java.util.List;

public interface MealRecordService {

    /**
     * 提交饮食反馈打卡
     */
    void submitFeedback(Long userId, MealFeedbackDTO feedbackDTO);

    /**
     * 获取用户今天的打卡记录
     */
    List<MealRecord> getTodayRecords(Long userId);
}
