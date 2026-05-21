package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询关联关系
     * @param dishIds
     * @return
     */
//  @Select("select setmeal_id from setmeal_dish where dish_id in (???)")
    List<Long> getByDishId(List<Long> dishIds);

    /**
     * 批量增加套餐菜品关联关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);
}
