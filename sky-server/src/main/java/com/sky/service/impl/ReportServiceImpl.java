package com.sky.service.impl;

import com.sky.dto.DailyTurnoverDTO;
import com.sky.dto.DailyUserDTO;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
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
    @Autowired
    private UserMapper userMapper;

    /**
     * 根据时间统计营业额
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 公共校验并生成日期列表
        List<LocalDate> dateList = generateDateListAndValidate(begin, end);

        // 修正时间范围（确保覆盖 end 日全天）
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = end.plusDays(1).atStartOfDay().minusNanos(1);

        // 查询日营业额
        List<DailyTurnoverDTO> dailyTurnoverDTOS = orderMapper.selectDailyTurnoverList(beginTime, endTime, 5);
        Map<LocalDate, Double> turnoverMap = dailyTurnoverDTOS.stream()
                .collect(Collectors.toMap(DailyTurnoverDTO::getOrderDate, DailyTurnoverDTO::getSum));

        List<Double> turnoverList = fillDataList(dateList, turnoverMap, 0.0);

        return TurnoverReportVO.builder()
                .dateList(joinList(dateList))
                .turnoverList(joinList(turnoverList))
                .build();
    }

    /**
     * 根据时间统计用户数量
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 1. 校验日期并生成日期列表
        List<LocalDate> dateList = generateDateListAndValidate(begin, end);

        // 2. 修正时间范围（覆盖 end 日全天）
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = end.plusDays(1).atStartOfDay().minusNanos(1);

        // 3. 查询每日新增用户数
        List<DailyUserDTO> newUserDTOs = userMapper.selectDailyNewUsers(beginTime, endTime);
        Map<LocalDate, Integer> newUserMap = newUserDTOs.stream()
                .collect(Collectors.toMap(DailyUserDTO::getDate, DailyUserDTO::getCount));

        // 4. 使用 fillDataList 填充每日新增用户数
        List<Integer> newUserCounts = fillDataList(dateList, newUserMap, 0);

        // 5. 计算累计用户数（直接内联逻辑）
        List<Integer> totalUserCounts = new ArrayList<>();
        int cumulativeTotal = 0;
        for (Integer count : newUserCounts) {
            cumulativeTotal += count;
            totalUserCounts.add(cumulativeTotal);
        }

        // 6. 构建返回结果
        return UserReportVO.builder()
                .dateList(joinList(dateList))
                .newUserList(joinList(newUserCounts))
                .totalUserList(joinList(totalUserCounts))
                .build();
    }

    // 生成日期列表并校验
    private List<LocalDate> generateDateListAndValidate(LocalDate begin, LocalDate end) {
        Objects.requireNonNull(begin, "开始日期不能为空");
        Objects.requireNonNull(end, "结束日期不能为空");
        if (begin.isAfter(end)) {
            throw new IllegalArgumentException("日期范围错误");
        }
        return begin.datesUntil(end.plusDays(1)).toList();
    }

    // 泛型填充方法
    private <T> List<T> fillDataList(
            List<LocalDate> dateList,
            Map<LocalDate, T> dataMap,
            T defaultValue
    ) {
        return dateList.stream()
                .map(date -> dataMap.getOrDefault(date, defaultValue))
                .toList();
    }

    // 泛型拼接方法
    private <T> String joinList(List<T> list) {
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}