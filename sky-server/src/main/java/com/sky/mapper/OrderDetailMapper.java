package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.core.annotation.Order;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单详细数据
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);

    /**
     * 根据订单id查询订单详细信息
     * @param id
     * @return
     */
    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> queryByOrderId(Long id);

    /**
     * 根据订单id查询订单菜品信息
     * @param orderIds
     * @return
     */
    List<OrderDetail> queryDishesByOrderIds(List<Long> orderIds);
}
