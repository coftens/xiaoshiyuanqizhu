package com.xsyq.dto;

import lombok.Data;

import java.util.List;

@Data
public class WeeklyReportDTO {

    /**
     * 涵盖的总天数 (例如周报通常是 7)
     */
    private int totalDays;

    /**
     * 本周有按时打卡记录的天数
     */
    private int activeDays;

    /**
     * 本周总餐数 (按每天3餐推算是21餐)
     */
    private int totalMeals;

    /**
     * 按要求吃 / 吃得刚好 的餐数
     */
    private int suggestedMeals;

    /**
     * 吃多了的餐数
     */
    private int overateMeals;

    /**
     * 没吃饱的餐数
     */
    private int underateMeals;

    /**
     * AI基于一周打卡行为生成的洞察总结
     */
    private String aiSummary;

    /**
     * 每天的饮水完成情况
     */
    private List<DailyWaterStat> waterStats;

    @Data
    public static class DailyWaterStat {
        private String date;
        private int currentMl;
        private int targetMl;
    }
}
