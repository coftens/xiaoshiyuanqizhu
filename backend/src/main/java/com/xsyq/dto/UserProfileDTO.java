package com.xsyq.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UserProfileDTO {
    private Integer gender; // 1:男 2:女
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private Integer age;
    private String goal; // LOSE_FAT, GAIN_MUSCLE, etc.
    private List<String> allergy;
    private List<String> disease;
    private List<String> dietPreference;
}
