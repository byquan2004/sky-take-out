package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openId
     * @return
     */
    @Select("select * from user where openid = #{openId}")
    User queryByOpenId(String openId);

    /**
     * 新建用户
     * @param newUser
     */
    void insert(User newUser);

    /**
     * 根据id查询用户信息
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);
}
