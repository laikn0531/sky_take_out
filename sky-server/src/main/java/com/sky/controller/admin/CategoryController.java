package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@RestController
@Slf4j
@Api(tags = "分类相关接口")
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增分类")
    public Result save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类: {}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("page")
    @ApiOperation(value = "分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询: {}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id删除分类
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "根据id删除分类")
    public Result<String> deleteById(Long id){
        log.info("根据id删除分类: {}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 根据id修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation(value = "根据id修改分类")
    public Result<String> updateById(@RequestBody CategoryDTO categoryDTO){
        log.info("根据id修改分类: {}", categoryDTO);
        categoryService.updateById(categoryDTO);
        return Result.success();
    }

    /**
     * 启用禁用分类
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation(value = "启用禁用分类")
    public Result<String> startOrStop(@PathVariable Integer status, Long id){
        log.info("启用禁用分类: {}, {}", status, id );
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "根据类型查询分类")
    public Result<List> list(Integer type){
        log.info("根据类型查询分类: {}", type);
        List<Category> categoryList = categoryService.list(type);
        return Result.success(categoryList);
    }
}
