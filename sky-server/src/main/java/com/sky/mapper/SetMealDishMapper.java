package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 查询菜品关联套餐
     */
    List<SetmealDish> queryByDishIds(List<Long> ids);

    /**
     * 新增菜品关联套餐
     * @param setmealDishes
     */
    void insert(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除关联菜品
     * @param ids
     */
    void removeBySetmealIds(List<Long> ids);

    /**
     * 根据套餐id查询关联餐品信息
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> queryBySetmealId(Long id);
}
