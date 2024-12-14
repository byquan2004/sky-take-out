package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据餐品分类查询菜品
     */
    @Select("select count(*) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into dish(name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) values" +
            "(#{name}, #{categoryId},#{price},#{image},#{description},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Dish dish);

    /**
     * 分页查询菜品数据
     * @param dishPageQueryDTO
     * @return
     */
    List<DishVO> list(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 统计起售菜品数量
     * @param ids
     * @return
     */
    Long countEnableByIds(List<Long> ids);

    /**
     * 鼻梁删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据id获取菜品信息
     * @param id
     */
    @Select("select  * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 更新菜品信息
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);
}
