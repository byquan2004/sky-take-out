package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.PageResult;

public interface CategoryService {

    /**
     * 分类分页查询
     * @param pageQueryDTO
     * @return
     */
    PageResult page(CategoryPageQueryDTO pageQueryDTO);

    /**
     * 新增菜品分类或套餐分类
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 根据id删除对应分类
     * @param id
     */
    void remove(Long id);

    /**
     * 修改分类信息
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 启用或禁用分类
     * @param status
     * @param id
     */
    void isDisable(Integer status, Long id);

    /**
     * 根据类型查询分类
     * @param categoryDTO
     * @return
     */
    PageResult listForCategory(CategoryDTO categoryDTO);
}
