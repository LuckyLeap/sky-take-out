package com.sky.service.impl;

import com.sky.dto.DailyTurnoverDTO;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 根据时间统计营业额
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 参数校验
        Objects.requireNonNull(begin, "开始日期不能为空");
        Objects.requireNonNull(end, "结束日期不能为空");
        if (begin.isAfter(end)) throw new IllegalArgumentException("日期范围错误");

        // 生成日期列表
        List<LocalDate> dateList = begin.datesUntil(end.plusDays(1)).toList();

        // 修正时间范围（确保覆盖 end 日全天）
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = end.plusDays(1).atStartOfDay().minusNanos(1);

        List<DailyTurnoverDTO> dailyTurnoverDTOS = orderMapper.selectDailyTurnoverList(beginTime, endTime, 5);
        Map<LocalDate, Double> turnoverMap = dailyTurnoverDTOS.stream()
                .collect(Collectors.toMap(DailyTurnoverDTO::getOrderDate, DailyTurnoverDTO::getSum));

        List<Double> turnoverList = dateList.stream()
                .map(date -> turnoverMap.getOrDefault(date, 0.0))
                .toList();

        return TurnoverReportVO.builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .turnoverList(turnoverList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .build();
    }
}