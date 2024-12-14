package com.sky.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserLoginImpl implements UserService {

    private final JwtProperties jwtProperties;

    private final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final WeChatProperties weChatProperties;

    private final UserMapper userMapper;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        // 调用微信接口获取openid(响应json字符串)
        String res = wxLoginGetOpenId(userLoginDTO);
        JSONObject jsonObject = JSON.parseObject(res);
        String openId = jsonObject.getString("openid");
        // 判断openid是否存在
        if(openId == null) throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        // 不存在直接注册用户
        User user = userMapper.queryByOpenId(openId);
        if(user == null) {
            user = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        // 创建jw
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        UserLoginVO loginVo = UserLoginVO.builder()
                .openid(openId)
                .id(user.getId())
                .token(token)
                .build();
        return loginVo;
    }

    /**
     * 微信登陆获取openid
     * @param userLoginDTO
     * @return
     */
    private String wxLoginGetOpenId(UserLoginDTO userLoginDTO) {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("appid",weChatProperties.getAppid());
        paramMap.put("secret",weChatProperties.getSecret());
        paramMap.put("js_code", userLoginDTO.getCode());
        paramMap.put("grant_type","authorization_code");
        return HttpClientUtil.doGet(WX_LOGIN_URL, paramMap);
    }
}
