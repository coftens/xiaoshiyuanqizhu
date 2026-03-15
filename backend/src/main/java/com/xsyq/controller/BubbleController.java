package com.xsyq.controller;

import com.xsyq.common.Result;
import com.xsyq.dto.BubbleVO;
import com.xsyq.security.CustomUserDetails;
import com.xsyq.service.BubbleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bubble")
@RequiredArgsConstructor
public class BubbleController {

    private final BubbleService bubbleService;

    /**
     * 获取首页 AI 气泡评语 (贴士)
     *
     * @param clientTime 客户端当前时间，格式：HH:mm，如果不传则默认使用服务器当前时间
     * @param currentSteps 当前最新手机步数，默认0
     * @param statusPatch 当前状态补丁(NORMAL, EXAM, PERIOD, SICK, WORKOUT等)，默认NORMAL
     */
    @GetMapping("/today")
    public Result<BubbleVO> getHomeBubble(
            @RequestParam(required = false) String clientTime,
            @RequestParam(required = false, defaultValue = "0") Integer currentSteps,
            @RequestParam(required = false, defaultValue = "NORMAL") String statusPatch,
            Authentication authentication) {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        BubbleVO bubble = bubbleService.generateHomeBubble(userId, clientTime, currentSteps, statusPatch);
        return Result.success(bubble);
    }
}