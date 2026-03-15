package com.xsyq.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class ScheduleConfigDTO {
    private LocalTime morningStart;
    private LocalTime morningEnd;
    private Integer morningPeriods;

    private LocalTime afternoonStart;
    private LocalTime afternoonEnd;
    private Integer afternoonPeriods;

    private Boolean eveningEnabled;
    private LocalTime eveningStart;
    private LocalTime eveningEnd;
    private Integer eveningPeriods;
}
