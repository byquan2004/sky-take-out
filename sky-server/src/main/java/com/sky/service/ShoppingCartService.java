package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    void save(ShoppingCartDTO shoppingCartDTO);

    /**
     * 获取当前用户购物车商品
     * @return
     */
    List<ShoppingCart> list();

    /**
     * 清空当前用户购物车
     * @return
     */
    void clean();
}
