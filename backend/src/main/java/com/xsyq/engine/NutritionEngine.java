package com.xsyq.engine;

import com.xsyq.dto.DailyNutritionDTO;
import com.xsyq.entity.Schedule;
import com.xsyq.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class NutritionEngine {

    /**
     * 根据用户档案、课程表列表，计算出当天的营养分配目标
     */
    public DailyNutritionDTO computeDailyTargets(UserProfile profile, List<Schedule> todaysSchedule) {
        DailyNutritionDTO dto = new DailyNutritionDTO();

        // 1. 计算 BMR (Mifflin-St Jeor)
        double weight = profile.getWeightKg().doubleValue();
        double height = profile.getHeightCm().doubleValue();
        int age = profile.getAge();
        
        int bmr;
        if (profile.getGender() == 1) { // 男
            bmr = (int) Math.round(10 * weight + 6.25 * height - 5 * age + 5);
        } else { // 女
            bmr = (int) Math.round(10 * weight + 6.25 * height - 5 * age - 161);
        }
        dto.setBmr(bmr);

        // 2. 根据课表计算基础活动系数 PAL
        double pal = 1.2; // 默认久坐
        boolean hasSports = false;

        if (todaysSchedule != null) {
            for (Schedule sc : todaysSchedule) {
                String type = sc.getCourseType();
                if ("CULTURE".equals(type)) pal += 0.02; // 每节文化课
                else if ("SCIENCE".equals(type)) pal += 0.04; // 每节理科课
                else if ("SPORTS".equals(type)) {
                    pal += 0.15; // 体育课高耗能
                    hasSports = true;
                }
            }
        }
        
        // 运动日当日额外补贴一点消耗
        if (hasSports) pal += 0.1;
        
        // 四舍五入保留两位小数
        BigDecimal palDecimal = new BigDecimal(pal).setScale(2, RoundingMode.HALF_UP);
        dto.setPal(palDecimal);

        // 3. 计算 TDEE
        int tdee = (int) Math.round(bmr * palDecimal.doubleValue());
        dto.setTdee(tdee);

        // 4. 计算目标热量 (根据减脂/增肌目标调整)
        int targetCal = tdee;
        if ("LOSE_FAT".equalsIgnoreCase(profile.getGoal())) {
            targetCal -= 300; // 温和减脂缺口
        } else if ("GAIN_MUSCLE".equalsIgnoreCase(profile.getGoal())) {
            targetCal += 250; // 增肌盈余
        }
        
        // 防止热量过低
        if (targetCal < bmr) {
            targetCal = bmr;
        }
        dto.setTargetCal(targetCal);

        // 5. 宏量营养素分配
        // 蛋白质默认: 体重 * 1.5g (减脂增肌不同，此处简化用 1.8，日常用 1.2)
        double proteinRatio = "GAIN_MUSCLE".equals(profile.getGoal()) || "LOSE_FAT".equals(profile.getGoal()) ? 1.8 : 1.2;
        int proteinG = (int) Math.round(weight * proteinRatio);
        
        // 脂肪默认占总热量 25% （1g脂肪=9kcal）
        int fatG = (int) Math.round((targetCal * 0.25) / 9.0);
        
        // 碳水填补剩下热量 (1g碳水=4kcal, 1g蛋白=4kcal)
        int carbCal = targetCal - (proteinG * 4) - (fatG * 9);
        int carbG = (int) Math.round(carbCal / 4.0);

        dto.setProteinG(proteinG);
        dto.setFatG(fatG);
        dto.setCarbG(carbG);

        // 6. 基础三餐热量拆分分配（早30，午40，晚30）
        dto.setBreakfastCal((int)(targetCal * 0.3));
        dto.setLunchCal((int)(targetCal * 0.4));
        dto.setDinnerCal(targetCal - dto.getBreakfastCal() - dto.getLunchCal());

        // 7. 饮水量推荐（体重*30ml + 体育课修正）
        int water = (int)(weight * 30);
        if (hasSports) water += 500;
        dto.setWaterMl(water);

        return dto;
    }
}
