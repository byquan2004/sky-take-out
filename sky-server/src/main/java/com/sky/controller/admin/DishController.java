package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @CacheEvict(value = "dishCache", allEntries = true)
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
     * 根据菜品ids批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "dishCache", allEntries = true)
    public Result remove(@RequestParam List<Long> ids) {
        log.info("批量删除菜品:{}", ids);
        dishService.remove(ids);
        return Result.success();
    }

    /**
     * 根据id获取菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> getInfo(@PathVariable Long id){
        log.info("根据id获取菜品信息:{}", id);
        return Result.success(dishService.getInfo(id));
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @PutMapping
    @CacheEvict(value = "dishCache", allEntries = true)
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息:{}", dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }

    /**
     * 修改菜品状态
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "dishCache", allEntries = true)
    public Result isDisable(@PathVariable String status, @RequestParam String id){
        log.info("修改当前{}菜品状态",id);
        dishService.isDisable(Integer.valueOf(status), Long.valueOf(id));
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     */
    @GetMapping("/list")
    public Result<List<DishVO>> listForCategory(@RequestParam Integer categoryId) {
        log.info("根据分类id查询菜品:{}", categoryId);
        DishPageQueryDTO dishPageQueryDTO = new DishPageQueryDTO();
        dishPageQueryDTO.setCategoryId(categoryId);
        dishPageQueryDTO.setStatus(StatusConstant.ENABLE);
        return Result.success(dishService.listWithFlavor(dishPageQueryDTO));
    }
}
