package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品并分配口味
     * @param dishDTO
     */
    void save(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult list(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据菜品id批量删除菜品
     * @param ids
     * @return
     */
    void remove(List<Long> ids);
}
