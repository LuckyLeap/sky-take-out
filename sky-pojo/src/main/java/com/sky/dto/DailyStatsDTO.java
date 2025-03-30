package com.sky.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class DailyStatsDTO {
    private LocalDate date;
    // 每日订单总数
    private Integer orderCount = 0; // 默认值
    // 每日有效订单数
    private Integer validCount = 0; // 默认值

    public DailyStatsDTO(LocalDate date) {
        this.date = date;
        this.orderCount = 0;
        this.validCount = 0;
    }
}