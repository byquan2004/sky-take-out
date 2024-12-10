package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    /**
     * 新增菜品并分配口味
     * @param dishDTO
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品:{}", dishDTO);
        dishService.save(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> list(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        return Result.success(dishService.list(dishPageQueryDTO));
    }

    /**
     * 根据菜品id批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result remove(@RequestParam List<Long> ids) {
        log.info("批量删除菜品:{}", ids);
        dishService.remove(ids);
        return Result.success();
    }
}
