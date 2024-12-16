package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
    public List<SetmealVO> list(Setmeal setmeal) {
        setmeal.setStatus(StatusConstant.ENABLE);
        List<SetmealVO> list = setMealMapper.list(setmeal);
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

    /**
     * 添加套餐
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(StatusConstant.DISABLE);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes == null) throw new BaseException(MessageConstant.SETMEAL_DISH_IS_NULL);
        // 新增完套餐后需要返回主键id
        setMealMapper.insert(setmeal);
        setmealDishes.forEach(s -> s.setSetmealId(setmeal.getId()));
        setMealDishMapper.insert(setmealDishes);
    }

    /**
     * 根据ids删除套餐
     * @param ids
     */
    @Transactional
    @Override
    public void remove(List<Long> ids) {
        // 查询起售套餐数量 条件成立不允许删除
        Long countBySetMeal = setMealMapper.queryBySetmealIds(ids);
        if(countBySetMeal > 0) throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);

        // 删除套餐 并 删除套餐关联菜品记录
        setMealMapper.removeByIds(ids);
        setMealDishMapper.removeBySetmealIds(ids);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealPageQueryDTO, setmeal);
        List<SetmealVO> setmealList = setMealMapper.list(setmeal);
        Page<SetmealVO> page = (Page<SetmealVO>) setmealList;
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据套餐id查询套餐信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO = setMealMapper.getById(id);
        List<SetmealDish> setmealDishes = setMealDishMapper.queryBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     * @return
     */
    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setMealMapper.update(setmeal);
        List<SetmealDish> setMealDishes = setmealDTO.getSetmealDishes();
        if(setMealDishes == null) return;
        // 删除套餐关联菜品数据 再重新添加关联菜品信息
        setMealDishMapper.removeBySetmealIds(Collections.singletonList(setmeal.getId()));
        setMealDishes.forEach(s -> s.setSetmealId(setmeal.getId()));
        setMealDishMapper.insert(setMealDishes);
    }

    /**
     * 起售套餐
     * @param status
     * @param id
     * @return
     */
    @Override
    public void isEnable(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .status(status)
                .id(id)
                .build();
        setMealMapper.update(setmeal);
    }
}