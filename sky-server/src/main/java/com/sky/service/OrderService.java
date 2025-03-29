package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.*;

public interface OrderService {

    /**
     * 用户下单
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO);

    /**
     * 支付成功，修改订单状态
     */
    void paySuccess(String outTradeNo);

    /**
     * 历史订单查询
     */
    PageResult<OrderVO> pageQuery4User(int page, int pageSize, Integer status);

    /**
     * 订单详情查询
     */
    OrderVO details(Long id);

    /**
     * 用户取消订单
     */
    void userCancelById(Long id);

    /**
     * 再来一单
     */
    void repetition(Long id);

    /**
     * 客户催单
     */
    void reminder(Long id);

    /**
     * 管理端-条件搜索订单
     */
    PageResult<OrderVO> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 管理端-各个状态的订单数量统计
     */
    OrderStatisticsVO statistics();

    /**
     * 管理端-接单
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 管理端-拒单
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 管理端-订单取消
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 管理端-派送订单
     */
    void delivery(Long id);

    /**
     * 管理端-完成订单
     */
    void complete(Long id);
}