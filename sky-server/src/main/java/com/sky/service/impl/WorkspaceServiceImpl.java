package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.*;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final DishMapper dishMapper;
    private final SetMealMapper setmealMapper;
    private final ReportMapper reportMapper;

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        /**
         * 营业额：当日已完成订单的总金额
         * 有效订单：当日已完成订单的数量
         * 订单完成率：有效订单数 / 总订单数
         * 平均客单价：营业额 / 有效订单数
         * 新增用户：当日新增用户的数量
         */

        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);

        //查询总订单数
        Integer totalOrderCount = reportMapper.countOrderByDateAndStatus(begin,end, null);

        map.put("status", Orders.COMPLETED);
        //营业额
        Double turnover = reportMapper.sumByMap(map);
        turnover = turnover == null? 0.0 : turnover;

        //有效订单数
        Integer validOrderCount = reportMapper.countOrderByDateAndStatus(begin, end, Orders.COMPLETED);

        Double unitPrice = 0.0;

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0 && validOrderCount != 0){
            //订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            //平均客单价
            unitPrice = turnover / validOrderCount;
        }

        //新增用户数
        Integer newUsers = reportMapper.countUserByDate(begin,end);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView() {
        Map map = new HashMap();
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));
        map.put("status", Orders.TO_BE_CONFIRMED);

        //待接单
        Integer waitingOrders = reportMapper.countOrderByDateAndStatus(
                LocalDateTime.now().with(LocalTime.MIN),
                null, Orders.TO_BE_CONFIRMED);

        //待派送
        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = reportMapper.countOrderByDateAndStatus(
                LocalDateTime.now().with(LocalTime.MIN),
                null, Orders.CONFIRMED
        );

        //已完成
        map.put("status", Orders.COMPLETED);
        Integer completedOrders = reportMapper.countOrderByDateAndStatus(
                LocalDateTime.now().with(LocalTime.MIN),
                null, Orders.COMPLETED
        );

        //已取消
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = reportMapper.countOrderByDateAndStatus(
                LocalDateTime.now().with(LocalTime.MIN),
                null, Orders.CANCELLED
        );

        //全部订单
        map.put("status", null);
        Integer allOrders = reportMapper.countOrderByDateAndStatus(
                LocalDateTime.now().with(LocalTime.MIN),
                null, null
        );

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    public DishOverViewVO getDishOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    public SetmealOverViewVO getSetmealOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
