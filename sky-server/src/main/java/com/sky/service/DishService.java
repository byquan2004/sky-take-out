package com.sky.service;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

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

    /**
     * 根据id获取菜品信息
     * @param id
     */
    DishVO getInfo(Long id);

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @AutoFill(OperationType.UPDATE)
    void update(DishDTO dishDTO);

    /**
     * 修改菜品状态
     * @param status
     * @param id
     */
    void isDisable(Integer status, Long id);
}
