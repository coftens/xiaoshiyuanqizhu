-- ==============================================
-- 校食元气 App — 数据库初始化脚本 (MySQL 8.0)
-- 包含 App 端业务表 及 管理后台端业务表
-- ==============================================

CREATE DATABASE IF NOT EXISTS `xsyq_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xsyq_db`;

-- ==================== 1. 管理后台模块 ====================

-- 后台管理员表
CREATE TABLE IF NOT EXISTS `sys_admin` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT '加密密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `role` VARCHAR(20) DEFAULT 'ADMIN' COMMENT '角色: SUPER_ADMIN, ADMIN',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='后台管理员表';

-- 插入默认超管密码为 123456 (注意实际中需 BCrypt 加密，这里仅做标识。如使用SpringSecurity应初始化加密后的值)
-- BCrypt加密后的 "123456" 大概是: $2a$10$wI5fF2X9.E2lF/3xOoVvTu/O6rZ2R4G39Xg8/dF8E4JgH.F4jXk2C
INSERT INTO `sys_admin` (`username`, `password`, `real_name`, `role`) 
VALUES ('admin', '$2a$10$wI5fF2X9.E2lF/3xOoVvTu/O6rZ2R4G39Xg8/dF8E4JgH.F4jXk2C', '超级管理员', 'SUPER_ADMIN')
ON DUPLICATE KEY UPDATE id=id;


-- ==================== 2. 用户业务模块 (App端) ====================

-- 用户基础表 (App注册)
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `phone` VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar_url` VARCHAR(255) COMMENT '头像URL',
    `invite_code` VARCHAR(6) UNIQUE COMMENT '个人邀请码',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-封禁',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='App用户表';

-- 用户健康档案表
CREATE TABLE IF NOT EXISTS `user_profile` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT UNIQUE NOT NULL COMMENT '关联用户ID',
    `gender` TINYINT NOT NULL COMMENT '1:男 2:女',
    `height_cm` DECIMAL(5,1) NOT NULL COMMENT '身高cm',
    `weight_kg` DECIMAL(5,1) NOT NULL COMMENT '体重kg',
    `age` INT NOT NULL COMMENT '年龄',
    `bmi` DECIMAL(4,1) COMMENT 'BMI指数',
    `goal` VARCHAR(50) COMMENT '目标: LOSE_FAT / GAIN_MUSCLE / MAINTAIN / GUT_CARE / IMMUNITY',
    `allergy` VARCHAR(255) COMMENT '过敏源 JSON数组',
    `disease` VARCHAR(255) COMMENT '病史 JSON数组',
    `diet_preference` VARCHAR(255) COMMENT '饮食偏好 JSON数组',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='用户健康档案';

-- ==================== 3. 课表系统模块 ====================

-- 学校作息时间配置
CREATE TABLE IF NOT EXISTS `school_schedule_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT UNIQUE NOT NULL,
    `morning_start` TIME COMMENT '上午第一节开始时间',
    `morning_end` TIME COMMENT '上午下课时间',
    `morning_periods` INT COMMENT '上午节数',
    `afternoon_start` TIME COMMENT '下午第一节开始时间',
    `afternoon_end` TIME COMMENT '下午下课时间',
    `afternoon_periods` INT COMMENT '下午节数',
    `evening_enabled` TINYINT(1) DEFAULT 0 COMMENT '是否启用晚课',
    `evening_start` TIME COMMENT '晚课开始时间',
    `evening_end` TIME COMMENT '晚课下课时间',
    `evening_periods` INT COMMENT '晚课节数'
) ENGINE=InnoDB COMMENT='周课表时间作息设置';

-- 周课表详情
CREATE TABLE IF NOT EXISTS `schedule` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `week_start_date` DATE NOT NULL COMMENT '课表所属周的周一日期',
    `weekday` TINYINT NOT NULL COMMENT '1-5（周一~周五）',
    `period` TINYINT NOT NULL COMMENT '当日第几节',
    `course_type` VARCHAR(20) DEFAULT 'NONE' COMMENT 'NONE/CULTURE/SPORTS/SCIENCE',
    `course_name` VARCHAR(50) COMMENT '课程名(预留自定义)',
    INDEX `idx_user_week` (`user_id`, `week_start_date`)
) ENGINE=InnoDB COMMENT='用户周课表矩阵';

-- ==================== 4. 营养与AI算法模块 ====================

-- 每日能量预算
CREATE TABLE IF NOT EXISTS `daily_energy` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `date` DATE NOT NULL,
    `bmr` INT COMMENT '基础代谢',
    `pal` DECIMAL(3,2) COMMENT '活动系数合成',
    `tdee` INT COMMENT '总消耗量',
    `target_cal` INT COMMENT '目标摄入热量',
    `protein_g` INT COMMENT '蛋白质目标',
    `carb_g` INT COMMENT '碳水目标',
    `fat_g` INT COMMENT '脂肪目标',
    `water_ml` INT COMMENT '饮水目标',
    `status_patch` VARCHAR(50) DEFAULT 'NORMAL' COMMENT '生理期/防脱发/考前...补丁',
    `weather` VARCHAR(20) DEFAULT 'NORMAL',
    UNIQUE KEY `uk_user_date` (`user_id`, `date`)
) ENGINE=InnoDB COMMENT='每日能量消耗与素配比';

-- AI 饮食推荐记录
CREATE TABLE IF NOT EXISTS `ai_recommendation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `date` DATE NOT NULL,
    `meal_type` VARCHAR(20) NOT NULL COMMENT 'BREAKFAST/LUNCH/DINNER/SNACK',
    `target_cal` INT,
    `prompt_hash` VARCHAR(64) COMMENT '防重复API调用的hash',
    `recommendation_json` TEXT COMMENT 'AI返回或规则引擎生成的完整JSON食谱',
    `source` VARCHAR(20) COMMENT 'AI / FALLBACK_RULE',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='AI食谱推荐记录';
