package com.sky.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyTurnoverDTO {
    private LocalDate orderDate;
    private Double sum;
}