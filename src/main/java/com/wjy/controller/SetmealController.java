package com.wjy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
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
       //避免后台修改数据前端不访问数据库产生脏数据，清理一下redis已经缓存的数据
       String Key="Setmeal_"+setmealDto.getCategoryId()+"_1";
       stringRedisTemplate.delete(Key);
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
            //redis
            LambdaUpdateWrapper<Setmeal> wrapper2 = new LambdaUpdateWrapper<>();
            wrapper2.eq(Setmeal::getId,ids);
            Long categoryId = setmealService.getOne(wrapper2).getCategoryId();
            //避免后台修改数据前端不访问数据库产生脏数据，清理一下redis已经缓存的数据
            String Key="Setmeal_"+categoryId+"_1";
            stringRedisTemplate.delete(Key);
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
            //redis
            LambdaUpdateWrapper<Setmeal> wrapper2 = new LambdaUpdateWrapper<>();
            wrapper2.eq(Setmeal::getId,ids);
            Long categoryId = setmealService.getOne(wrapper2).getCategoryId();
            //避免后台修改数据前端不访问数据库产生脏数据，清理一下redis已经缓存的数据
            String Key="Setmeal_"+categoryId+"_1";
            stringRedisTemplate.delete(Key);
        }
        return R.success("起售成功");
    }
    //套餐的删除
    @DeleteMapping
    public R<String> deleteSetmeal(@RequestParam(value = "ids")List<Long> ids){
       log.info(ids.toString());
        for (Long id : ids) {
            //redis
            LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Setmeal::getId,id);
            Long categoryId = setmealService.getOne(wrapper).getCategoryId();
            //避免后台修改数据前端不访问数据库产生脏数据，清理一下redis已经缓存的数据
            String Key="Setmeal_"+categoryId+"_1";
            stringRedisTemplate.delete(Key);
        }
        return setmealService.removeWithDish(ids);
    }


    //移动端index页面显示
    @GetMapping("/list")
    public R<List<Setmeal>> getdishlist(@RequestParam(value="categoryId")Long categoryId,
                                        @RequestParam(value = "status",required = false)String status
    ){
        List<Setmeal> setmeals = new ArrayList<>();
        String Key="Setmeal_"+categoryId+"_"+status;
        //先从redis中获取缓存数据
        String json = stringRedisTemplate.opsForValue().get(Key);
        //如果redis存在，则直接返回
        if(JSON.parseObject(json, new TypeReference<List<Setmeal>>(){})!=null){
            setmeals= JSON.parseObject(json, new TypeReference<List<Setmeal>>(){});
            return  R.success(setmeals);
        }
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getCategoryId,categoryId).eq(Setmeal::getStatus,status);
        List<Setmeal> list = setmealService.list(wrapper);
        setmeals=list;
        //如果不存在的情况，将查询出的菜品数据缓存到redis
        stringRedisTemplate.opsForValue().set(Key,JSON.toJSONString(setmeals),60, TimeUnit.MINUTES);
        return  R.success(setmeals);
    }
}
