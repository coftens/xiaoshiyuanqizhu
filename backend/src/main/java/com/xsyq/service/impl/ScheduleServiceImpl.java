package com.xsyq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xsyq.dto.DailyNutritionDTO;
import com.xsyq.dto.ScheduleConfigDTO;
import com.xsyq.dto.ScheduleItemDTO;
import com.xsyq.engine.NutritionEngine;
import com.xsyq.entity.Schedule;
import com.xsyq.entity.SchoolScheduleConfig;
import com.xsyq.entity.UserProfile;
import com.xsyq.mapper.ScheduleMapper;
import com.xsyq.mapper.SchoolScheduleConfigMapper;
import com.xsyq.mapper.UserProfileMapper;
import com.xsyq.service.ScheduleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private SchoolScheduleConfigMapper configMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;
    
    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private NutritionEngine nutritionEngine;

    @Override
    @Transactional
    public void saveScheduleConfig(Long userId, ScheduleConfigDTO dto) {
        LambdaQueryWrapper<SchoolScheduleConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolScheduleConfig::getUserId, userId);
        SchoolScheduleConfig config = configMapper.selectOne(wrapper);

        if (config == null) {
            config = new SchoolScheduleConfig();
            config.setUserId(userId);
            BeanUtils.copyProperties(dto, config);
            configMapper.insert(config);
        } else {
            BeanUtils.copyProperties(dto, config);
            configMapper.updateById(config);
        }
    }

    @Override
    @Transactional
    public void saveScheduleMatrix(Long userId, LocalDate weekStart, List<ScheduleItemDTO> items) {
        // 先删除该用户该周的旧课表记录
        LambdaQueryWrapper<Schedule> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(Schedule::getUserId, userId)
                     .eq(Schedule::getWeekStartDate, weekStart);
        scheduleMapper.delete(deleteWrapper);

        // 插入新的课表（忽略NONE类型的空白记录以节省空间）
        for (ScheduleItemDTO item : items) {
            if ("NONE".equalsIgnoreCase(item.getCourseType()) || item.getCourseType() == null) {
                continue; 
            }
            Schedule schedule = new Schedule();
            schedule.setUserId(userId);
            schedule.setWeekStartDate(weekStart);
            schedule.setWeekday(item.getWeekday());
            schedule.setPeriod(item.getPeriod());
            schedule.setCourseType(item.getCourseType());
            schedule.setCourseName(item.getCourseName());
            scheduleMapper.insert(schedule);
        }
    }

    @Override
    public DailyNutritionDTO calculateDailyNutrition(Long userId, LocalDate date) {
        // 1. 获取用户档案
        LambdaQueryWrapper<UserProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(profileWrapper);
        if (profile == null) {
            throw new RuntimeException("请先完成身体档案注册");
        }

        // 2. 推算这一天的 "周一日期" 和 "星期几" (1-5)
        int dayOfWeek = date.getDayOfWeek().getValue();
        // 如果是周末，暂不计入特殊加成（返回基础消耗）
        List<Schedule> todaysSchedule = null;
        if (dayOfWeek <= 5) {
            LocalDate weekStart = date.minusDays(dayOfWeek - 1);
            LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
            scheduleWrapper.eq(Schedule::getUserId, userId)
                           .eq(Schedule::getWeekStartDate, weekStart)
                           .eq(Schedule::getWeekday, dayOfWeek);
            todaysSchedule = scheduleMapper.selectList(scheduleWrapper);
        }

        // 3. 将档案和打平当天的课表交给引擎进行精密推理
        return nutritionEngine.computeDailyTargets(profile, todaysSchedule);
    }
}
