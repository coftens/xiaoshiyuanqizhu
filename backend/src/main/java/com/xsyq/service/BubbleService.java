package com.xsyq.service;

import com.xsyq.dto.BubbleVO;

public interface BubbleService {
    
    /**
     * 根据用户当前的状态、时间、数据，生成专属的首页 AI 气泡评语
     *
     * @param userId 用户ID
     * @param clientTime 前端传来的当前时间（格式 HH:mm），用于判断早中晚等
     * @param currentSteps 当前步数（由前端健康授权获取并传入，默认可传0）
     * @param statusPatch 今日状态补丁（如：NORMAL, PERIOD, EXAM, SICK, WORKOUT 等）
     * @return 生成的气泡信息
     */
    BubbleVO generateHomeBubble(Long userId, String clientTime, Integer currentSteps, String statusPatch);
}