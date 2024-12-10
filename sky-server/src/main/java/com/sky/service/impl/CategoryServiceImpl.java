package com.sky.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import javassist.LoaderClassPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    /**
     * 分类分页查询
     * 设置分页参数,执行查询,返回结果
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult page(CategoryPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        List<Category> categories = categoryMapper.list(pageQueryDTO);
        Page<Category> pages = (Page<Category>)categories;
        return new PageResult(pages.getTotal(), pages.getResult());
    }

    /**
     * 新增菜品分类或套餐分类
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        category.setStatus(StatusConstant.DISABLE);
        categoryMapper.save(category);
    }

    /**
     * 根据id删除对应分类
     * @param id
     */
    @Override
    public void remove(Long id) {
        categoryMapper.remove(id);
    }

    /**
     * 修改分类信息
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.update(category);
    }

    /**
     * 启用或禁用分类
     * @param status
     * @param id
     * @return
     */
    @Override
    public void isDisable(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
    }

    /**
     * 根据类型查询分类
     * @param categoryDTO
     * @return
     */
    @Override
    public PageResult listForCategory(CategoryDTO categoryDTO) {
        PageHelper.startPage(1, 100);
        List<Category> categories = categoryMapper.queryByType(categoryDTO.getType());
        Page<Category> pages = (Page<Category>) categories;
        return new PageResult(pages.getTotal(), pages.getResult());
    }
}
