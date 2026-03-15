package com.xsyq.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleMatrixRequestDTO {
    private LocalDate weekStartDate;
    private List<ScheduleItemDTO> items;
}
