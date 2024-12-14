package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;

    private final DishFlavorMapper dishFlavorMapper;

    private final SetMealDishMapper setMealDishMapper;

    /**
     * 新增菜品并分配口味
     * @param dishDTO
     */
    @Transactional
    @Override
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(StatusConstant.DISABLE);
        // 添加菜品 并 拿到id字段
        dishMapper.insert(dish);
        // 添加口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors.size() <= 0) return;
        flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
        dishFlavorMapper.insertBatch(flavors);
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult list(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        List<DishVO> list = dishMapper.list(dishPageQueryDTO);
        Page<DishVO> dishes = (Page<DishVO>) list;
        return new PageResult(dishes.getTotal(), dishes.getResult());
    }

    /**
     * 根据菜品id批量删除菜品
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public void remove(List<Long> ids) {
        // 起售状态 不能删除 提示错误
        Long countStatus =  dishMapper.countEnableByIds(ids);
        if(countStatus > 0) throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);

        // 菜品与套餐关联 不能删除 提示错误
        List<SetmealDish> setmealDishes = setMealDishMapper.queryByDishIds(ids);
        if(setmealDishes.size() > 0) throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);

        // 删除菜品 和 菜品口味
        dishFlavorMapper.deleteByDishIds(ids);
        dishMapper.deleteByIds(ids);
    }

    /**
     * 根据id获取菜品信息
     * @param id
     */
    @Override
    public DishVO getInfo(Long id) {
        // 获取菜品基本信息
        Dish dish = dishMapper.getById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        // 获取菜品口味信息
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        if(dishVO != null) dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @Transactional
    @Override
    public void update(DishDTO dishDTO) {
        // 更新菜品基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        // 删除菜品口味表的数据 重新添加
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors.isEmpty()) return;
        flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
        dishFlavorMapper.deleteByDishIds(Collections.singletonList(dish.getId()));
        dishFlavorMapper.insertBatch(flavors);
    }

    /**
     * 修改菜品状态
     * @param status
     * @param id
     */
    @Override
    public void isDisable(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dishPageQueryDTO: categoryId, status
     * @return
     */
    public List<DishVO> listWithFlavor(DishPageQueryDTO dishPageQueryDTO) {
        List<DishVO> list = dishMapper.list(dishPageQueryDTO);
        for (DishVO d : list) {
            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());
            d.setFlavors(flavors);
        }
        return list;
    }
}
