package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询套餐id
     * @param dishIds
     * @return
     */
//  @Select("select setmeal_id from setmeal_dish where dish_id in (???)")
    List<Long> getByDishId(List<Long> dishIds);
}
