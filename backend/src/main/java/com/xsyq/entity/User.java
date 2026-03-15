package com.xsyq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String nickname;

    private String avatarUrl;

    private String inviteCode;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
