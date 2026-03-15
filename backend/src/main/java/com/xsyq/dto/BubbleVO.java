package com.xsyq.dto;

import lombok.Data;

@Data
public class BubbleVO {
    /**
     * AI 气泡文字内容
     */
    private String content;

    /**
     * 当前触发的状态/时机（如：早晨、上课中、考试周、喝水达标）
     */
    private String triggerTiming;

    /**
     * 左上角的 Emoji 图标，根据不同情况给不同图标（如 ☀️, 💪, 🚶, 🧠）
     */
    private String icon;
}