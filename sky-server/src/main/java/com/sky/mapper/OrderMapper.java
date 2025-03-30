package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.*;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     */
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     */
    void update(Orders orders);

    /**
     * 根据订单号和用户id查询订单
     */
    @Select("select * from orders where number = #{orderNumber} and user_id = #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 分页条件查询并按下单时间排序
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 管理端-根据状态统计订单数量
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer toBeConfirmed);

    /**
     * 根据状态和下单时间查询订单
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 查询每日的营业额
     */
    List<DailyTurnoverDTO> selectDailyTurnoverList(
            @Param("begin") LocalDateTime begin,
            @Param("end") LocalDateTime end,
            @Param("status") Integer status
    );

    /**
     * 订单每日统计
     */
    List<DailyStatsDTO> getDailyStats(
            @Param("begin") LocalDateTime begin,
            @Param("end") LocalDateTime end
    );

    /**
     * 订单总统计
     */
    TotalStatsDTO getTotalStats(
            @Param("begin") LocalDateTime begin,
            @Param("end") LocalDateTime end
    );

    /**
     * 查询销量Top10商品
     */
    List<GoodsSalesDTO> getSalesTop10(
            @Param("begin") LocalDateTime begin,
            @Param("end") LocalDateTime end
    );
}