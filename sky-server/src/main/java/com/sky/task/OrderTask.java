package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务
 */
@Slf4j
@Component
public class OrderTask {

    @Resource
    private OrderMapper orderMapper;

    /**
     * 处理订单超时任务
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processOrderTimeout(){
        log.info("处理订单超时:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        List<Orders> orderList = orderMapper.queryOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if(orderList != null){
            orderList.forEach(item -> {
                item.setCancelReason("订单超时,自动取消");
                item.setCancelTime(LocalDateTime.now());
                item.setStatus(Orders.CANCELLED);
                orderMapper.update(item);
            });
        }
    }

    /**
     * 处理订单一直处于派送状态
     * 每天凌晨1点执行一次
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processOrderDelivery(){
        log.info("处理订单一直处于派送状态:{}", LocalDateTime.now());
        // 凌晨0:00
        LocalDateTime time = LocalDateTime.now().minusHours(1);
        List<Orders> orderList = orderMapper.queryOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if(orderList != null){
            orderList.forEach(item -> {
                item.setStatus(Orders.COMPLETED);
                orderMapper.update(item);
            });
        }
    }
}
