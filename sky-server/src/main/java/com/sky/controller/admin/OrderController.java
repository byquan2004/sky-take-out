package com.sky.controller.admin;

import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 取消订单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("取消订单:{}", ordersRejectionDTO);
        orderService.cancel(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 统计各状态订单数量
     * @return
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        return Result.success(orderService.statistics());
    }

    /**
     * 完成订单
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        log.info("完成订单:{}", id);
        orderService.complete(id);
        return Result.success();
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒单:{}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 接单
     * @param
     * @return
     */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接单:{}", ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    public Result<OrderVO> detail(@PathVariable Long id){
        log.info("查询订单{}详情",id);
        return Result.success(orderService.detail(id));
    }

    /**
     * 派送订单
     * @param id
     */
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id){
        log.info("派送订单:{}", id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 条件查询订单列表
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult> list(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("条件查询订单列表:{}",ordersPageQueryDTO);
        return Result.success(orderService.list(ordersPageQueryDTO));
    }
}
