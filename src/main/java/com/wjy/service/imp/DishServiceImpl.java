package com.wjy.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wjy.common.R;
import com.wjy.dto.DishDto;
import com.wjy.entity.Dish;
import com.wjy.entity.DishFlavor;
import com.wjy.mapper.DishMapper;
import com.wjy.service.DishFlavorService;
import com.wjy.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Transactional
    //需要在启动类上开启事务的支持
    //加在方法上，该方法内的所有dao操作都作为一个事务
    //加载类上，该类的所有方法都作为一个事务
    //为什么这个注解不加在dao层
    /*
    因为一个Service完成一个服务，但是可能会调用很多个DAO层的功能，如果Transaction放在DAO层的话，
    做完一个DAO，就会提交一次事务，永久修改数据库，后面在调用另外一个DAO，但是throws Exception，
    对于整个的Service来说，应该是要完全回滚的，但是只能回滚到当前的DAO，所以这就破坏了事务的ACID，
    所以事务是加在Service层的。
    * */
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        //在保存菜品的基本信息到表dish的时候，id自动生成了，
        //但是在口味表的dish_id默认是null，所以需要进行赋值操作
        Long dishId = dishDto.getId();//菜品id

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
//        flavors = flavors.stream().map((item) -> {
//            item.setDishId(dishId);
//            return item;
//        }).collect(Collectors.toList());
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
         //先更新菜品的基本信息
        this.updateById(dishDto);
        Long dishID = dishDto.getId();
        //因为还更新了口味，所以先获取该菜的口味
        //条件查询，需要查询哪个字段，就直接访问他在实体类对应的属性
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getId,dishDto.getId());
        //因为也同时更改了口味，可以将原先口味表中对应菜品的所有口味删除，重新加入新的菜品对应的口味
        dishFlavorService.remove(wrapper);
        for (DishFlavor flavor : dishDto.getFlavors()) {
            flavor.setDishId(dishID);
            dishFlavorService.save(flavor);
        }
        //        //更新dish表基本信息
//        this.updateById(dishDto);
//
//        //清理当前菜品对应口味数据---dish_flavor表的delete操作
//        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
//        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
//
//        dishFlavorService.remove(queryWrapper);
//
//        //添加当前提交过来的口味数据---dish_flavor表的insert操作
//        List<DishFlavor> flavors = dishDto.getFlavors();
//
//        flavors = flavors.stream().map((item) -> {
//            item.setDishId(dishDto.getId());
//            return item;
//        }).collect(Collectors.toList());
//
//        dishFlavorService.saveBatch(flavors);
    }
}
