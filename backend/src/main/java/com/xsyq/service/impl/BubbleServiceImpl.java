package com.xsyq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xsyq.dto.BubbleVO;
import com.xsyq.entity.MealRecord;
import com.xsyq.entity.Schedule;
import com.xsyq.entity.UserProfile;
import com.xsyq.mapper.MealRecordMapper;
import com.xsyq.mapper.ScheduleMapper;
import com.xsyq.mapper.UserProfileMapper;
import com.xsyq.service.BubbleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.DayOfWeek;

@Slf4j
@Service
@RequiredArgsConstructor
public class BubbleServiceImpl implements BubbleService {

    private final UserProfileMapper userProfileMapper;
    private final ScheduleMapper scheduleMapper;
    private final MealRecordMapper mealRecordMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aliyun.ai.api-key:}")
    private String apiKey;

    @Value("${aliyun.ai.model:qwen-plus}")
    private String aiModel;

    @Override
    public BubbleVO generateHomeBubble(Long userId, String clientTime, Integer currentSteps, String statusPatch) {
        BubbleVO bubble = new BubbleVO();
        
        // 1. 获取时间上下文
        LocalTime time = null;
        try {
            if (StringUtils.hasText(clientTime)) {
                time = LocalTime.parse(clientTime, DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                time = LocalTime.now();
            }
        } catch (Exception e) {
            time = LocalTime.now();
        }

        String timeContext = determineTimeContext(time);
        bubble.setTriggerTiming(timeContext);

        // 2. 获取当天课表情况 (当前是周几)
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        int dayOfWeekVal = dayOfWeek.getValue(); // 1-7
        
        LambdaQueryWrapper<Schedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(Schedule::getUserId, userId).eq(Schedule::getWeekday, dayOfWeekVal);
        List<Schedule> todaySchedules = scheduleMapper.selectList(scheduleWrapper);
        String scheduleContext = generateScheduleContext(todaySchedules);

        // 3. 获取用户身体特征与健康目标
        LambdaQueryWrapper<UserProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(profileWrapper);
        String userContext = (profile != null) ? 
            String.format("健康目标:%s, BMI:%s", profile.getGoal(), profile.getBmi()) : "未知";

        // 4. 组装输入给 AI 的 Prompt
        String systemPrompt = "你是一位极懂大学生的贴心健康饮食AI助手。请以发一条“朋友圈心情/朋友发文字对话”般简短、温暖、带点调皮的语气，生成一句推送气泡提示。字数强硬控制在50个字以内，只能一句话。不要输出包括由于、或者Markdown的多余格式。";
        
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("此时状态: ").append(timeContext).append("\n");
        userPrompt.append("当天步数: ").append(currentSteps == null ? 0 : currentSteps).append("步\n");
        userPrompt.append("今日课业: ").append(scheduleContext).append("\n");
        userPrompt.append("用户身体情况: ").append(userContext).append("\n");
        userPrompt.append("状态补丁: ").append(StringUtils.hasText(statusPatch) ? statusPatch : "正常状态").append("\n");
        
        userPrompt.append("请结合以上信息，只输出一句鼓励的温馨小贴士（50字内，参考风格：'今天满课一天呀！记得早餐多吃点慢消化的碳水，下午才没那么容易犯困哦~' 或 '下午没课，适合去校园散散步！目前步数 ").append(currentSteps==null?0:currentSteps).append(" 步，加油！'）");

        // 5. 默认 Icon 根据时间上下文来判断
        bubble.setIcon(determineIcon(timeContext, statusPatch));

        // 6. 调用阿里云 AI 大模型
        try {
            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("model", aiModel);
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt.toString()));
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
            String aiContent = rootNode.path("choices").get(0).path("message").path("content").asText().replace("\n", "");
            bubble.setContent(aiContent);

        } catch (Exception e) {
            log.error("AI 气泡生成失败", e);
            bubble.setContent(getFallbackContent(timeContext)); // 降级处理
        }

        return bubble;
    }

    private String determineTimeContext(LocalTime time) {
        int hour = time.getHour();
        if (hour >= 6 && hour < 9) return "早晨打开App";
        if (hour >= 9 && hour < 12) return "上午期间";
        if (hour >= 12 && hour < 14) return "午餐前";
        if (hour >= 14 && hour < 18) return "下午空闲/上课";
        if (hour >= 18 && hour < 21) return "晚上";
        return "深夜修仙";
    }

    private String determineIcon(String timing, String statusPatch) {
        if ("EXAM".equalsIgnoreCase(statusPatch)) return "🧠";
        if ("PERIOD".equalsIgnoreCase(statusPatch)) return "❤️";
        if ("WORKOUT".equalsIgnoreCase(statusPatch)) return "🏃";
        
        switch (timing) {
            case "早晨打开App": return "☀️";
            case "上午期间": return "💪";
            case "午餐前": return "💡";
            case "下午空闲/上课": return "🚶";
            case "晚上": return "🥦";
            case "深夜修仙": return "🌙";
            default: return "🤖";
        }
    }

    private String generateScheduleContext(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return "今天没安排课";
        }
        long courseCount = schedules.stream().filter(s -> s.getCourseType() != null && !s.getCourseType().equals("NONE")).count();
        if (courseCount >= 4) {
            return "今天是满课状态，课表很满！";
        } else if (courseCount > 0) {
            return "今天有 " + courseCount + " 节大课";
        } else {
            return "今天基本没有课找时间放松";
        }
    }

    private String getFallbackContent(String timing) {
        switch (timing) {
            case "早晨打开App": return "早上好~ 早餐是开启一天元气的关键，吃点好的奖励自己吧！";
            case "上午期间": return "上午好！注意多喝水，适时站起来活动一下颈椎哦。";
            case "午餐前": return "快下课啦！今天的午餐吃什么好呢，记得挑优质蛋白质。";
            case "下午空闲/上课": return "下午如果容易犯困，可以安排一些站立或者低强度的活动~";
            case "晚上": return "晚上好，今天辛苦啦！晚餐吃点清淡的，减轻身体负担。";
            case "深夜修仙": return "夜深了，早点休息吧，熬夜伤身，明天再继续加油！";
            default: return "今天也要好好吃饭呀。";
        }
    }
}