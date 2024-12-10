package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryController {


    private final CategoryService categoryService;

    /**
     * 新增菜品分类或套餐分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类:{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 查询分类列表
     */
    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO pageQueryDTO){
        log.info("分类的分页查询参数:{}",pageQueryDTO);
        PageResult page = categoryService.page(pageQueryDTO);
        return Result.success(page);
    }

    /**
     * 根据id删除对应分类
     * @param id
     * @return
     */
    @DeleteMapping
    public Result remove(@RequestParam Long id){
        log.info("根据id删除对应分类:{}", id);
        categoryService.remove(id);
        return Result.success();
    }

    /**
     * 修改分类信息
     * @param categoryDTO
     * @return
     */
    @PutMapping
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改分类信息:{}", categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 启用或禁用分类
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result isDisable(@PathVariable String status, @RequestParam String id) {
        log.info("启用或禁用分类:{},{}", status, id);
        categoryService.isDisable(Integer.valueOf(status), Long.valueOf(id));
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @param categoryDTO
     * @return
     */
    @GetMapping("/list")
    public Result<PageResult> listForCategory(CategoryDTO categoryDTO){
        log.info("根据类型查询分类:{}",categoryDTO);
        return Result.success(categoryService.listForCategory(categoryDTO));
    }
}
