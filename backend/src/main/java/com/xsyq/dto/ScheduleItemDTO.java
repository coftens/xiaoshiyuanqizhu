package com.xsyq.dto;

import lombok.Data;

@Data
public class ScheduleItemDTO {
    private Integer weekday;
    private Integer period;
    private String courseType; // NONE, CULTURE, SPORTS, SCIENCE
    private String courseName;
}
