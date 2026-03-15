package com.xsyq.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Checking and initializing missing database tables...");

        String createMealRecordTable = "CREATE TABLE IF NOT EXISTS `meal_record` (" +
            "`id` bigint NOT NULL AUTO_INCREMENT," +
            "`user_id` bigint NOT NULL," +
            "`record_date` date NOT NULL," +
            "`meal_type` varchar(20) NOT NULL," +
            "`feedback_status` varchar(20) NOT NULL," +
            "`created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "`updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "PRIMARY KEY (`id`)," +
            "UNIQUE KEY `uk_user_date_meal` (`user_id`, `record_date`, `meal_type`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食打卡反馈表';";

        String createWaterRecordTable = "CREATE TABLE IF NOT EXISTS `water_record` (" +
            "`id` bigint NOT NULL AUTO_INCREMENT," +
            "`user_id` bigint NOT NULL," +
            "`record_date` date NOT NULL," +
            "`amount_ml` int NOT NULL DEFAULT 0," +
            "`created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "`updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "PRIMARY KEY (`id`)," +
            "UNIQUE KEY `uk_user_date` (`user_id`, `record_date`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮水打卡记录表';";

        try {
            jdbcTemplate.execute(createMealRecordTable);
            jdbcTemplate.execute(createWaterRecordTable);
            log.info("Database tables initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize database tables: ", e);
        }
    }
}
