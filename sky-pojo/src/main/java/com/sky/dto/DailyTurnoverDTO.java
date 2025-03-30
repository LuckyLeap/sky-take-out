package com.sky.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyTurnoverDTO {
    //日期
    private LocalDate orderDate;
    //营业额
    private Double sum;
}