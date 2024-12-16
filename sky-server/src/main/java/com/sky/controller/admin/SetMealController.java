package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("adminSetMealController")
@RequestMapping("/admin/setmeal")
@RequiredArgsConstructor
public class SetMealController {

    private final SetMealService setMealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setMealCache", allEntries = true)
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐:{}", setmealDTO);
        setMealService.save(setmealDTO);
        return Result.success();
    }

    /**
     * 删除套餐
     */
    @DeleteMapping
    @CacheEvict(value = "setMealCache", allEntries = true)
    public Result remove(@RequestParam List<Long> ids){
        log.info("根据id删除套餐:{}",ids);
        setMealService.remove(ids);
        return Result.success();
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询:{}", setmealPageQueryDTO);
        return Result.success(setMealService.page(setmealPageQueryDTO));
    }

    /**
     * 根据套餐id查询套餐信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查询套餐信息:{}", id);
        return Result.success(setMealService.getById(id));
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setMealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐信息:{}", setmealDTO);
        setMealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 起售套餐
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setMealCache", allEntries = true)
    public Result isEnable(@PathVariable Integer status, @RequestParam String id){
        log.info("套餐{}起售:{}", id,status);
        setMealService.isEnable(status, Long.valueOf(id));
        return Result.success();
    }

}
