package com.sky.mapper;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 新增菜品分类或套餐分类
     * @param category
     */
    @Insert("insert into category(name, sort,type,status, create_time, update_time, create_user, update_user ) " +
            "values (#{name}, #{sort}, #{type},#{status}, #{createTime}, #{updateTime},#{createUser}, #{updateUser})")
    void save(Category category);

    /**
     * 分类分页查询
     * @param pageQueryDTO
     * @return
     */
    List<Category> list(CategoryPageQueryDTO pageQueryDTO);

    /**
     * 根据id删除分类
     * @param id
     */
    @Delete("delete from category where id = #{id}")
    void remove(Long id);

    /**
     * 修改分类信息
     * @param category
     */
    void update(Category category);

    /**
     * 根据类型查询分类列表
     * @param type
     * @return
     */
    List<Category> queryByType(Integer type);
}
