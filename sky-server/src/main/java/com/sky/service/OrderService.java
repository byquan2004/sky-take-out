package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 取消订单
     * @param ordersRejectionDTO
     * @return
     */
    void cancel(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 统计各订单状态数量
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 完成订单
     * @param id
     */
    void complete(Long id);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    OrderVO detail(Long id);

    /**
     * 派送订单
     * @param id
     */
    void delivery(Long id);

    /**
     * 条件查询订单列表
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult list(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 用户取消订单
     * @param id
     */
    void userCancelOrder(Long id);

    /**
     * 再来一单
     * @param id
     * @return
     */
    void repetition(Long id);

    /**
     * 最近1个月订单记录
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult recentOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 催单
     * @param id
     */
    void reminder(Long id);
}
