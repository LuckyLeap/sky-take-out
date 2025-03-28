package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单
 */
@RestController("userOrderController")
@RequestMapping("/user/order")
@Tag(name = "C端-订单接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     */
    @PostMapping("/submit")
    @Operation(summary = "用户下单", description = "用户下单")
    public Result<OrderSubmitVO> submit(@Validated @RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     */
    @PutMapping("/payment")
    @Operation(summary = "订单支付", description = "订单支付")
    public Result<OrderPaymentVO> payment(@Validated @RequestBody OrdersPaymentDTO ordersPaymentDTO){
        log.info("订单支付：{}", ordersPaymentDTO);
        try {
            OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
            log.info("生成预支付交易单：{}", orderPaymentVO);
            return Result.success(orderPaymentVO);
        } catch (Exception e) {
            // 记录异常日志
            log.error("订单支付失败，参数：{}，异常信息：{}", ordersPaymentDTO, e.getMessage(), e);
            // 返回友好的错误信息
            return Result.error("订单支付失败，请稍后重试");
        }
    }

    /**
     * 历史订单查询
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */
    @GetMapping("/historyOrders")
    @Operation(summary = "历史订单查询", description = "历史订单查询")
    public Result<PageResult<OrderVO>> page(int page, int pageSize, Integer status) {
        PageResult<OrderVO> pageResult = orderService.pageQuery4User(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @Operation(summary = "查询订单详情", description = "查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 用户取消订单
     */
    @PutMapping("/cancel/{id}")
    @Operation(summary = "用户取消订单", description = "用户取消订单")
    public Result<String> cancel(@PathVariable("id") Long id) {
        orderService.userCancelById(id);
        return Result.success();
    }

    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    @Operation(summary = "再来一单", description = "再来一单")
    public Result<String> repetition(@PathVariable Long id) {
        orderService.repetition(id);
        return Result.success();
    }
}