package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@RequiredArgsConstructor
public class shopController {

    private final String SHOP_STATUS = "SHOP_STATUS";

    private final RedisTemplate<Object, Object> redisTemplate;

    /**
     * 获取当前店铺营业状态
     * @return
     */
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        if (!redisTemplate.hasKey(SHOP_STATUS)) {
            redisTemplate.opsForValue().set(SHOP_STATUS, 0);
        }
        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        log.info("获取当前店铺营业状态:{}",shopStatus == 1? "营业中" : "休息中");
        return Result.success(shopStatus);
    }

    /**
     * 修改店铺营业状态
     * @return
     */
    @PutMapping("/{status}")
    public Result update(@PathVariable Integer status) {
        redisTemplate.opsForValue().set(SHOP_STATUS, status);
        log.info("修改店铺营业状态:{}",status == 1? "营业中" : "休息中");
        return Result.success();
    }
}
