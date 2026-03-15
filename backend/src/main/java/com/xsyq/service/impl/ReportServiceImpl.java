package com.xsyq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xsyq.dto.WeeklyReportDTO;
import com.xsyq.entity.MealRecord;
import com.xsyq.entity.WaterRecord;
import com.xsyq.mapper.MealRecordMapper;
import com.xsyq.mapper.WaterRecordMapper;
import com.xsyq.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final MealRecordMapper mealRecordMapper;
    private final WaterRecordMapper waterRecordMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aliyun.ai.api-key:}")
    private String apiKey;

    @Value("${aliyun.ai.model:qwen-plus}")
    private String aiModel;

    @Override
    public WeeklyReportDTO generateWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(6); // 过去7天(包含今天)

        WeeklyReportDTO report = new WeeklyReportDTO();
        report.setTotalDays(7);

        // 1. 查询过去7天的所有三餐打卡记录
        LambdaQueryWrapper<MealRecord> mealWrapper = new LambdaQueryWrapper<>();
        mealWrapper.eq(MealRecord::getUserId, userId)
                   .between(MealRecord::getRecordDate, startOfWeek, today);
        List<MealRecord> mealRecords = mealRecordMapper.selectList(mealWrapper);

        int suggestedCount = 0;
        int overateCount = 0;
        int underateCount = 0;
        
        // 算出“活跃天数” （只要每天有一次打卡就算这天活跃）
        long activeDaysCount = mealRecords.stream().map(MealRecord::getRecordDate).distinct().count();
        report.setActiveDays((int) activeDaysCount);
        report.setTotalMeals(mealRecords.size());

        for (MealRecord rec : mealRecords) {
            switch (rec.getFeedbackStatus()) {
                case "AS_SUGGESTED": suggestedCount++; break;
                case "OVERATE": overateCount++; break;
                case "UNDERATE": underateCount++; break;
            }
        }
        report.setSuggestedMeals(suggestedCount);
        report.setOverateMeals(overateCount);
        report.setUnderateMeals(underateCount);

        // 2. 补全这7天的喝水数据
        LambdaQueryWrapper<WaterRecord> waterWrapper = new LambdaQueryWrapper<>();
        waterWrapper.eq(WaterRecord::getUserId, userId)
                    .between(WaterRecord::getRecordDate, startOfWeek, today);
        List<WaterRecord> waterList = waterRecordMapper.selectList(waterWrapper);

        List<WeeklyReportDTO.DailyWaterStat> waterStats = new ArrayList<>();
        // 从老到新填充
        for (int i = 0; i <= 6; i++) {
            LocalDate d = startOfWeek.plusDays(i);
            WeeklyReportDTO.DailyWaterStat stat = new WeeklyReportDTO.DailyWaterStat();
            stat.setDate(d.toString());
            stat.setTargetMl(2000); // 先写死2000，或者从UserProfile按体重算(kg * 30ml)

            Integer currentMl = waterList.stream()
                .filter(w -> w.getRecordDate().equals(d))
                .findFirst()
                .map(WaterRecord::getAmountMl)
                .orElse(0);

            stat.setCurrentMl(currentMl);
            waterStats.add(stat);
        }
        report.setWaterStats(waterStats);

        // 3. 调用 AI 生成专属周评
        String aiInsight = requestAiInsight(suggestedCount, overateCount, underateCount, activeDaysCount);
        report.setAiSummary(aiInsight);

        return report;
    }

    private String requestAiInsight(int suggestedCount, int overateCount, int underateCount, long activeDays) {
        try {
            String systemPrompt = "你是一位幽默专业的校园健康分析师。不要输出任何MarkDown格式。";
            String userPrompt = String.format(
                "请根据某大学生这周的饮食打卡数据，生成一段50字以内温馨的周报评语与下周鼓励。" +
                "本周坚持打卡了 %d 天，其中 %d 餐完美执行，吃多了 %d 餐，没吃饱 %d 餐。请给他一些建议。",
                activeDays, suggestedCount, overateCount, underateCount
            );

            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("model", aiModel);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));
            reqBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + apiKey);
            headers.add("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(reqBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
                entity,
                String.class
            );

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.path("choices").get(0).path("message").path("content").asText().replace("\n", "");

        } catch (Exception e) {
            return "本周干得不错哦，周末可以适当奖励自己一下~下周我们继续保持健康目标！💪";
        }
    }
}
