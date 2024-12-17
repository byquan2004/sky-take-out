package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.OrderConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.exception.OrderBusinessException;
import com.sky.properties.WeChatProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    private final ShoppingCartMapper shoppingCartMapper;

    private final AddressBookMapper addressBookMapper;

    private final UserMapper userMapper;

    private final OrderDetailMapper orderDetailMapper;

    private final WeChatPayUtil weChatPayUtil;

    private final WebSocketServer webSocketServer;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        // 抛出业务异常(购物车为空,地址为空)
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 向订单表插入数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setAddress(addressBook.getDetail());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setUserId(userId);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        User user = userMapper.getById(userId);
        orders.setUserName(user.getName());

        orderMapper.insert(orders); //返回主键id
        // 向订单明细表插入数据(可能一单多份餐)

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        // 清空购物车
        shoppingCartMapper.deleteByUserId(userId);
        // 封装返回VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        Map<String, Object> map = new HashMap<>();
        map.put("type", OrderConstant.REMINDER_STORE);
        map.put("orderId", orders.getId());
        map.put("content", "订单号:"+orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 取消订单
     * @param ordersRejectionDTO
     * @return
     */
    @Override
    public void cancel(OrdersRejectionDTO ordersRejectionDTO) {
        // todo 付款订单需要商家退款
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 统计各订单状态数量
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        // 待接单数量
        Integer toBeConfirmCount = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        // 待派送数量(已接单)
        Integer confirmedCount = orderMapper.countByStatus(Orders.CONFIRMED);
        // 派送中数量
        Integer deliveryInProgressCount = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);
        return OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmCount)
                .confirmed(confirmedCount)
                .deliveryInProgress(deliveryInProgressCount)
                .build();
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(orders);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        orders.setStatus(Orders.CONFIRMED);
        orders.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1)); // 预计送达时间
        orderMapper.update(orders);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO detail(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        List<OrderDetail> orderDetails = orderDetailMapper.queryByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        orders.setDeliveryStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    /**
     * 条件查询订单列表
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult list(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<OrderVO> page = getOrderVOList(ordersPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 历史订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 查询当前用户的订单
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<OrderVO> page = getOrderVOList(ordersPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 用户取消订单
     * @param id
     */
    @Override
    public void userCancelOrder(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);

        // 如果是待付款 可以直接取消订单
        if(orders.getStatus() != null && orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason(MessageConstant.ORDER_CANCEL_BY_USER);
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        } else {
            // todo 付款订单需要商家退款
            // 其他状态不能取消订单
            throw new OrderBusinessException(MessageConstant.ORDER_CANCEL_CONNECT_STORE);
        }
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @Override
    public void repetition(Long id) {
        // 查询历史订单
        Orders orders = orderMapper.getById(id);
        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        // 可以考虑地址更换 (这里只收到一个id不做此功能)
        OrdersSubmitDTO ordersSubmitDTO = new OrdersSubmitDTO();
        BeanUtils.copyProperties(orders,ordersSubmitDTO);
        this.submit(ordersSubmitDTO);
    }

    /**
     * 最近1个月订单记录
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult recentOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 查询当前用户的订单
        ordersPageQueryDTO.setBeginTime(LocalDateTime.now().minusMonths(1));
        ordersPageQueryDTO.setEndTime(LocalDateTime.now());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<OrderVO> page = getOrderVOList(ordersPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 用户催单啦
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if(orders == null) throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        Map<String, Object> map = new HashMap<>();
        map.put("type", OrderConstant.REMINDER_USER);
        map.put("orderId", orders.getId());
        map.put("content", "订单号:"+orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 获取订单列表
     * @param ordersPageQueryDTO
     * @return
     */
    private Page<OrderVO> getOrderVOList(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<OrderVO> orderList = orderMapper.queryList(ordersPageQueryDTO);
        // 获取所有订单的ID
        List<Long> orderIds = orderList.stream().map(OrderVO::getId).collect(Collectors.toList());

        // 批量查询菜品详情
        List<OrderDetail> orderDetails = orderDetailMapper.queryDishesByOrderIds(orderIds);

        // 将菜品详情映射到对应的订单
        Map<Long, List<OrderDetail>> orderDetailMap = orderDetails.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        orderList.forEach(item -> {
            List<OrderDetail> details = orderDetailMap.getOrDefault(item.getId(), Collections.emptyList());
            item.setOrderDishes(details.stream()
                    .map(orderDetail -> orderDetail.getName() + "*" + orderDetail.getNumber())
                    .collect(Collectors.joining(",")));
        });
        return (Page<OrderVO>) orderList;
    }


}
