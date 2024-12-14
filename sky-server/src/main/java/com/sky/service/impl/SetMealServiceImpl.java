package com.sky.service.impl;

import com.sky.entity.Setmeal;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SetMealServiceImpl implements SetMealService {

    private final SetMealMapper setMealMapper;

    private final SetMealDishMapper setMealDishMapper;

    private final DishMapper dishMapper;

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setMealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setMealMapper.getDishItemBySetmealId(id);
    }
}