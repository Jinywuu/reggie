package com.wjy.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.wjy.common.CustomException;
import com.wjy.common.R;
import com.wjy.dto.SetmealDto;
import com.wjy.entity.Setmeal;
import com.wjy.entity.SetmealDish;
import com.wjy.mapper.SetmealMapper;
import com.wjy.service.SetmealDishService;
import com.wjy.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        log.info(setmealDto.toString());
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);
        //获取setmeal生成时自增产生的setmealid，赋值给setmeal_dish
        Long id = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish dish : setmealDishes) {
                dish.setSetmealId(id);
                setmealDishService.save(dish);
        }
       return;

//
//        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
//        setmealDishes.stream().map((item) -> {
//            item.setSetmealId(setmealDto.getId());
//            return item;
//        }).collect(Collectors.toList());
//
//        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
//        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public SetmealDto getByIdWithSetmealDish(Long id) {
        //没有setmealDto这张表
        SetmealDto dto = new SetmealDto();
        Setmeal setmeal = setmealService.getById(id);
        BeanUtils.copyProperties(setmeal,dto);
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(wrapper);
        dto.setSetmealDishes(list);
        return dto;
    }

    /**
     * 修改套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */

    @Override
    public void updateSetmealDto(SetmealDto setmealDto) {
        //先将基本信息保存
        this.updateById(setmealDto);
        LambdaUpdateWrapper<SetmealDish> wrapper = new LambdaUpdateWrapper<>();
        Long id = setmealDto.getId();
        //根据SetmealId查询到该套餐的所有菜品，然后删除
        wrapper.eq(SetmealDish::getSetmealId,id);
        setmealDishService.remove(wrapper);
        for (SetmealDish setmealDish : setmealDto.getSetmealDishes()) {
            setmealDish.setSetmealId(id);
            setmealDishService.save(setmealDish);
        }
    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Transactional
    public R<String> removeWithDish(List<Long> ids) {
//        //select count(*) from setmeal where id in (1,2,3) and status = 1
//        //查询套餐状态，确定是否可用删除
//        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
//
//        queryWrapper.in(Setmeal::getId,ids);
//        queryWrapper.eq(Setmeal::getStatus,1);
//        //这个count是框架提供的方法
//        int count = this.count(queryWrapper);
//        if(count > 0){
//            //如果不能删除，抛出一个业务异常
//            throw new CustomException("套餐正在售卖中，不能删除");
//        }
//
//        //如果可以删除，先删除套餐表中的数据---setmeal
//        this.removeByIds(ids);
//
//        //delete from setmeal_dish where setmeal_id in (1,2,3)
//        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
//        //删除关系表中的数据----setmeal_dish
//        setmealDishService.remove(lambdaQueryWrapper);
            //select count(*) from setmeal where id in (1,2,3) and status = 1
            //查询套餐状态，确定是否可用删除
            LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.in(Setmeal::getId,ids);
            queryWrapper.eq(Setmeal::getStatus,1);

            int count = this.count(queryWrapper);
            if(count > 0){
//                //如果不能删除，抛出一个业务异常
//                    throw new CustomException("套餐正在售卖中，不能删除");
                return R.error("套餐正在出售，请更改套餐状态");
            }

            //如果可以删除，先删除套餐表中的数据---setmeal
            this.removeByIds(ids);

            //delete from setmeal_dish where setmeal_id in (1,2,3)
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
            //删除关系表中的数据----setmeal_dish
            setmealDishService.remove(lambdaQueryWrapper);
            return R.success("删除成功");
    }
}
