package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    /**
     * 批量插入菜品对应的口味
     * @param flavors
     */
    void inserBatch(List<DishFlavor> flavors);
}
