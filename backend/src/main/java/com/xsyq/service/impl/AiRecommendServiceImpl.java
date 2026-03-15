package com.xsyq.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xsyq.dto.AiRecommendResDTO;
import com.xsyq.dto.DailyNutritionDTO;
import com.xsyq.entity.UserProfile;
import com.xsyq.service.AiRecommendService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiRecommendServiceImpl implements AiRecommendService {

    // 默认使用你提供的测试 Key
    @Value("${ai.qwen.api-key:sk-3b50dcc8bf3c4e3aa6f29ef70e902af8}")
    private String apiKey;

    private final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AiRecommendResDTO generateDailyRecommendation(UserProfile profile, DailyNutritionDTO target) {
        
        // 1. 构建系统提示词
        String systemPrompt = "你是一位懂大学生的AI校园营养师“校食元气”。\n" +
                "请根据用户的身体档案、今日能量消耗目标、以及可能存在的过敏源和饮食偏好，为他量身定制今天在中国大学食堂能买到的三餐搭配。\n" +
                "【重要要求】\n" +
                "1. 菜品必须接地气，是中国高校食堂常见的饭菜（如：杂粮饭、黄焖鸡、紫菜蛋花汤等），不要推荐西式高级轻食或自己无法制作的餐品。\n" +
                "2. 必须严格遵守他的过敏源、忌口和特殊偏好。\n" +
                "3. dailyComment（今日气泡评语）必须具备网感、像个贴心的朋友，且**字数严格控制在30个字以内**（因为App前台气泡空间很小，绝不能换行或过长）。\n" +
                "4. 返回结果只能是纯JSON，不能有任何多余文字和Markdown代码块标注（开头不要有```json）。\n" +
                "【JSON格式】\n" +
                "{\n" +
                "  \"dailyComment\": \"30字以内的贴心寄语，例如：早八辛苦啦！今天中午奖励自己加个大鸡腿补充优质蛋白！🍗\",\n" +
                "  \"meals\": [\n" +
                "    {\"mealName\": \"早餐\", \"recommendedFood\": \"菜品组合如：全麦包+白煮蛋+豆浆\", \"estimatedCalories\": 数字, \"reason\": \"不超过30字的推荐由于\"},\n" +
                "    {\"mealName\": \"午餐\", \"recommendedFood\": \"菜品组合\", \"estimatedCalories\": 数字, \"reason\": \"说明\"},\n" +
                "    {\"mealName\": \"晚餐\", \"recommendedFood\": \"菜品组合\", \"estimatedCalories\": 数字, \"reason\": \"说明\"}\n" +
                "  ]\n" +
                "}";

        // 2. 构建用户当前的真实状态 Prompt
        String genderStr = profile.getGender() == 1 ? "男生" : "女生";
        String userPrompt = String.format(
            "【用户档案】%s，年龄：%d，身高：%s cm，体重：%s kg，健康目标：%s\n" +
            "【今日算法目标计算结果】总摄入目标: %d kcal。蛋白质需: %d g，碳水需: %d g，脂肪需: %d g。\n" +
            "算法的三餐热量切分建议：早餐约 %d kcal，午餐约 %d kcal，晚餐约 %d kcal。\n" +
            "【饮食约束】过敏源：%s，饮食偏好：%s\n" +
            "请直接输出符合要求的 JSON。",
            genderStr, profile.getAge(), profile.getHeightCm().toString(), profile.getWeightKg().toString(), profile.getGoal(),
            target.getTargetCal(), target.getProteinG(), target.getCarbG(), target.getFatG(),
            target.getBreakfastCal(), target.getLunchCal(), target.getDinnerCal(),
            profile.getAllergy() != null ? profile.getAllergy() : "无",
            profile.getDietPreference() != null ? profile.getDietPreference() : "无"
        );

        // 3. 构建 HTTP 请求体
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "qwen-plus");
            
            ArrayNode messages = requestBody.putArray("messages");
            // System message
            ObjectNode sysMsg = objectMapper.createObjectNode();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);
            
            // User message
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            // 发起请求
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析返回包
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                String aiContent = rootNode.path("choices").get(0).path("message").path("content").asText();
                
                // 清理可能的 markdown 符号
                aiContent = aiContent.replace("```json", "").replace("```", "").trim();
                
                return objectMapper.readValue(aiContent, AiRecommendResDTO.class);
            } else {
                throw new RuntimeException("AI API 调用失败，状态码：" + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("生成饮食推荐失败: " + e.getMessage());
        }
    }
}
