package com.wjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjy.common.R;
import com.wjy.dto.DishDto;
import com.wjy.dto.SetmealDto;
import com.wjy.entity.*;
import com.wjy.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

//套餐管理
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
     private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
   @PostMapping
    public R<String> Insert(@RequestBody  SetmealDto setmealDto){
         log.info(setmealDto.toString());
      setmealService.saveWithDish(setmealDto);
       return R.success("新增成功");
   }
   @GetMapping("/page")
    public  R<Page> getpage(int page,int pageSize,String name){
       //构造分页构造器对象
       Page<Setmeal> page1 = new Page<>(page,pageSize);
       Page<SetmealDto> page2 = new Page<>();
       //构造条件查询
       LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
       //模糊查询条件
       wrapper.like(name!=null,Setmeal::getName,name);
       //添加排序条件
       wrapper.orderByDesc(Setmeal::getUpdateTime);
       //执行分页查询
       setmealService.page(page1,wrapper);
       //主页面显示了category_name所以还是用SetmealDto来页面回显
       //拷贝页面信息（不包括分页查询的结果，只拷贝分页信息）
       BeanUtils.copyProperties(page1,page2,"records");
       ArrayList<SetmealDto> list = new ArrayList<>();
       for (Setmeal record : page1.getRecords()) {
           SetmealDto dto = new SetmealDto();
           BeanUtils.copyProperties(record,dto);
           Long categoryId = record.getCategoryId();
           Category category = categoryService.getById(categoryId);
           if(category!=null){
               dto.setCategoryName(category.getName());
           }
           list.add(dto);
       }
       page2.setRecords(list);
       return R.success(page2);
   }


   //修改时的数据回显
    @GetMapping("/{id}")
    public R<SetmealDto> update(@PathVariable Long id){
        //复杂的业务逻辑，所以在SetmealServiceImpI中自定义函数来解决
       SetmealDto setmealDto = setmealService.getByIdWithSetmealDish(id);
        return R.success(setmealDto);
    }
   //修改保存
   @PutMapping
   public  R<String> update(@RequestBody SetmealDto setmealDto){
       //复杂的业务逻辑，所以在SetmealServiceImpI中自定义函数来解决
       setmealService.updateSetmealDto(setmealDto);
       return R.success("保存成功");
   }





    //停售
    @PostMapping("/status/0")
    public R<String> status(@RequestParam(value = "ids")List<Integer>arr){
        for (Integer ids : arr) {
//            new LambdaQueryWrapper<>()
            LambdaUpdateWrapper<Setmeal> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Setmeal::getId,ids).set(Setmeal::getStatus,0);
            setmealService.update(wrapper);
//            LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
//            wrapper.eq(Setmeal::getId,ids);
//            Setmeal setmeal = setmealService.getOne(wrapper);
//            setmeal.setStatus(0);
//            setmealService.update(setmeal,wrapper);
        }
       return R.success("停售成功");
    }
    //起售
    @PostMapping("/status/1")
    public R<String> status2(@RequestParam(value = "ids")List<Integer>arr){
        for (Integer ids : arr) {
//            new LambdaQueryWrapper<>()
            LambdaUpdateWrapper<Setmeal> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Setmeal::getId,ids).set(Setmeal::getStatus,1);
            setmealService.update(wrapper);
//            LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
//            wrapper.eq(Setmeal::getId,ids);
//            Setmeal setmeal = setmealService.getOne(wrapper);
//            setmeal.setStatus(0);
//            setmealService.update(setmeal,wrapper);
        }
        return R.success("起售成功");
    }





    //套餐的删除
    @DeleteMapping
    public R<String> deleteSetmeal(@RequestParam(value = "ids")List<Long> ids){
      log.info(ids.toString());
       //在不考虑是否在出售不能删除的时候的默认删除策略
       //        for (Integer id : ids) {
////            setmealService.removeWithDish();
//            setmealService.removeById(id);
//            LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
//            wrapper.eq(SetmealDish::getSetmealId,id);
//            setmealDishService.remove(wrapper);
//        }
        return setmealService.removeWithDish(ids);
    }


    //移动端index页面显示
    @GetMapping("/list")
    public R<List<Setmeal>> getdishlist(@RequestParam(value="categoryId")Long categoryId,
                                        @RequestParam(value = "status",required = false)String status
    ){
        log.info("categoryId:{}",categoryId);
        log.info("status:{}",status);
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getCategoryId,categoryId).eq(Setmeal::getStatus,status);
        Setmeal setmeal = setmealService.getOne(wrapper);
        List<Setmeal> setmeals = new ArrayList<>();
        setmeals.add(setmeal);
        return  R.success(setmeals);
    }





}
