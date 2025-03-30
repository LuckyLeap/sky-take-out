package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.dto.DailyUserDTO;
import com.sky.dto.TotalStatsDTO;
import com.sky.dto.DailyTurnoverDTO;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据时间段统计营业数据：
     *   营业额：当日已完成订单的总金额
     *   有效订单：当日已完成订单的数量
     *   订单完成率：有效订单数 / 总订单数
     *   平均客单价：营业额 / 有效订单数
     *   新增用户：当日新增用户的数量
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        // 1. 获取总订单统计
        TotalStatsDTO totalStats = orderMapper.getTotalStats(begin, end);
        int totalOrders = totalStats.getTotal() != null ? totalStats.getTotal() : 0;
        int validOrders = totalStats.getValid() != null ? totalStats.getValid() : 0;

        // 2. 计算营业额（已完成订单的总金额）
        double turnover = orderMapper.selectDailyTurnoverList(begin, end, 5).stream()
                .mapToDouble(DailyTurnoverDTO::getSum)
                .sum();

        // 3. 计算新增用户数
        int newUsers = userMapper.selectDailyNewUsers(begin, end).stream()
                .mapToInt(DailyUserDTO::getCount)
                .sum();

        // 4. 计算订单完成率
        double completionRate = totalOrders == 0 ? 0.0 :
                Math.round(validOrders * 10000.0 / totalOrders) / 10000.0;

        // 5. 计算平均客单价
        double unitPrice = validOrders == 0 ? 0.0 :
                Math.round(turnover * 100.0 / validOrders) / 100.0;

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrders)
                .orderCompletionRate(completionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 查询订单管理数据
     */
    public OrderOverViewVO getOrderOverView() {
        //待接单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        //待派送数量
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        //已完成数量
        Integer completed = orderMapper.countStatus(Orders.COMPLETED);
        //已取消数量
        Integer cancelled = orderMapper.countStatus(Orders.CANCELLED);
        //全部订单数
        Integer allOrders = orderMapper.countStatus(null);

        return OrderOverViewVO.builder()
                .waitingOrders(toBeConfirmed)
                .deliveredOrders(deliveryInProgress)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     */
    public DishOverViewVO getDishOverView() {
        Integer sold = dishMapper.countStatus(StatusConstant.ENABLE);

        Integer discontinued = dishMapper.countStatus(StatusConstant.DISABLE);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     */
    public SetmealOverViewVO getSetmealOverView() {
        Integer sold = setmealMapper.countStatus(StatusConstant.ENABLE);

        Integer discontinued = setmealMapper.countStatus(StatusConstant.DISABLE);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}