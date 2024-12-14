package com.sky.controller.user;

import com.sky.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("userShopController")
@RequestMapping("/user/shop")
@RequiredArgsConstructor
public class ShopController {

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
}
