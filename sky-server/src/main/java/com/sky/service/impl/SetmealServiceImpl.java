package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐,同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //向套餐表插入数据，利用 MyBatis 主键回显获取自增的套餐 ID (useGeneratedKey)
        setmealMapper.insert(setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId())); //把获取生成的套餐id遍历传入'套餐菜品关联关系表'
        //保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //把控制层传来的DTO中的page和pageSize，get出来，传进调用PageHelper的startPage里
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        //再调用mapper层的方法，把DTO传进去，这样跟在starPage后面的方法，会自动拼接limit分页查询语句，返回Page类型的集合（该集合继承ArrayList，同样实现List接口）
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        //最后把该Page对象的total和result，get出来传入new的PageResult对象里，返回给控制层
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            //先根据套餐id遍历查询套餐当前起售状态(status)，但为了代码可以复用，直接查询套餐所有信息(下方的修改前回显也用到了该getById方法)
            //再从包含所有信息的setmeal对象里面，把 status get出来
            Setmeal setmeal = setmealMapper.getById(id);
            //起售中的套餐不能删除
            if (setmeal.getStatus() == StatusConstant.ENABLE){
                //根据该status来判断，如果选中的套餐有一个的status为1(即起售中)，则不符合业务要求，抛出自定义异常，无法删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        //批量删除套餐表中的数据,用到了foreach动态SQL
        setmealMapper.deleteBatch(ids);
        //批量删除套餐菜品关系表中的数据，用到了foreach动态SQL
        setmealDishMapper.deleteBatch(ids);
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        // 根据id查询套餐信息
        Setmeal setmeal = setmealMapper.getById(id);
        // 在套餐菜品关系表中，根据套餐id查询套餐菜品关系信息
        List<SetmealDish> setmealDishes =setmealDishMapper.getBySetmealId(id);

        // new一个setmealVO用于封装返回给前端的信息
        SetmealVO setmealVO = new SetmealVO();
        // 套餐对象拷贝，并把关系信息set进套餐对象
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        // new一个setmeal对象，并将传来的DTO拷贝进去
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 修改套餐表，执行update
        setmealMapper.update(setmeal);

        // 套餐菜品的关联关系，先全删掉，再加
            // 删除套餐和菜品的关联关系，操作setmeal_dish表，执行delete
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
            // 和新增的操作一样，先把setmealDish对象get出来传入集合，
            // 再foreach遍历把空的套餐id(此时前端传过来的DTO是有id的，因为修改前的查询已经获取到了，不像新增的时候还需要主键回显)set进去
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(sd -> {
            sd.setSetmealId(setmeal.getId());
        });
            // 重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 起售停售套餐
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if (status == StatusConstant.ENABLE){
            // 根据套餐id，查出菜品信息，所以需要通过菜品表和套餐菜品关系表，联查
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            dishList.forEach(dish -> {
                if (dish.getStatus() == StatusConstant.DISABLE){
                    // 只要发现有一个菜品处于停售状态，直接抛出异常
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        }

        Setmeal setmeal = Setmeal.builder()
                                .status(status)
                                .id(id)
                                .build();

        //更改套餐状态(status)
        setmealMapper.update(setmeal);
    }
}
