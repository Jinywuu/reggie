package com.wjy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjy.common.R;
import com.wjy.dto.DishDto;
import com.wjy.entity.Category;
import com.wjy.entity.Dish;
import com.wjy.entity.DishFlavor;
import com.wjy.mapper.DishMapper;
import com.wjy.service.CategoryService;
import com.wjy.service.DishFlavorService;
import com.wjy.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    //操作redis需要注入的对象
    private StringRedisTemplate stringRedisTemplate;
//    @Autowired
//    private DishMapper dishMapper;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝//拷贝的是分页的信息，不是实体类的信息
        //并且此时的两个records属性对应的值不需要进行处理，所以忽略（一个是List<dish>,一个是List<dishDto>）
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = new ArrayList<>();
        for (Dish dish : records) {
            DishDto dishDto = new DishDto();
            //此时dishDto值为空，将已经有值dish拷贝到dishDto中
            BeanUtils.copyProperties(dish,dishDto);
            //通过注入categoryService对象，将dish的categoryid传入查询类型名字
            Long categoryId = dish.getCategoryId();
            Category category = categoryService.getById(categoryId);
            //将类型名字赋值给dishDto
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            list.add(dishDto);
        }
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
//        List<DishDto> list = records.stream().map((item) -> {
//            DishDto dishDto = new DishDto();
//
//            BeanUtils.copyProperties(item,dishDto);
//
//            Long categoryId = item.getCategoryId();//分类id
//            //根据id查询分类对象
//            Category category = categoryService.getById(categoryId);
//
//            if(category != null){
//                String categoryName = category.getName();
//                dishDto.setCategoryName(categoryName);
//            }
//            return dishDto;
//        }).collect(Collectors.toList());
//
//        dishDtoPage.setRecords(list);
//
//        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * 用于修改时，修改页面(add.html)数据的回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /*
    删除菜品
    * */
    @DeleteMapping
    public R<String> delete(@RequestParam(value = "ids") List<Integer>arr){
        for (Integer id : arr) {
            //先删除菜品
            dishService.removeById(id);
            //再删除口味
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId,id);
            dishFlavorService.remove(wrapper);
        }
        return R.success("菜品删除成功");
    }

    //停售
    @PostMapping("/status/0")
    public R<String> status(@RequestParam(value = "ids",required = true) List<Integer> ids ){
        log.info(ids.toString());
        for (Integer id : ids) {
            LambdaUpdateWrapper<Dish> wrapper1 = new LambdaUpdateWrapper<>();
            wrapper1.eq(Dish::getId,id).set(Dish::getStatus,0);
            dishService.update(wrapper1);
        }
            return R.success("停售成功");
    }
    //起售
    @PostMapping("/status/1")
    public R<String> status2(@RequestParam(value = "ids",required = true) List<Integer> ids ){
        log.info(ids.toString());
        for (Integer id : ids) {
            LambdaUpdateWrapper<Dish> wrapper1 = new LambdaUpdateWrapper<>();
            wrapper1.eq(Dish::getId,id).set(Dish::getStatus,1);
            dishService.update(wrapper1);
        }
        return R.success("起售成功");
    }


    //套餐管理(修改套餐时，用于回显该套餐内的所有菜品)
//    @GetMapping("/list")
//    public R<List<Dish>> getdishlist(@RequestParam(value="categoryId")Long categoryId,
//                                     @RequestParam(value = "status",required = false)String status
//    ){
//        log.info("categoryId:{}",categoryId);
//        log.info("status:{}",status);
//        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Dish::getCategoryId,categoryId);
//        if(status!=null){
//            wrapper.eq(Dish::getStatus,Long.valueOf(status));
//        }
//        List<Dish> list = dishService.list(wrapper);
//        return  R.success(list);
//    }
    @GetMapping("/list")
    public R<List<DishDto>> getdishlist(@RequestParam(value="categoryId")Long categoryId,
                                     @RequestParam(value = "status",required = false)String status
    ){
        List<DishDto> list = new ArrayList<>();
        //动态拼接redis键
        String Key="dish_"+categoryId+"_"+status;

        //先从redis中获取缓存数据
        //(StringRedisTemplte和FastJosn的序列化和反序列化实现复杂类型在opsForValue中的存取（菜品缓存到redis）)
        /*
        * 在这个示例中，我们使用了 JSON.toJSONString() 将 List<DishDto> 对象转换为 JSON 字符串，
        * 并将其存储在 Redis 中。然后，我们使用 JSON.parseObject() 从 Redis 中检索并反序列化 JSON 字符串，
        * 以还原原始对象。请注意，我们使用了 TypeReference 来指定要反序列化的对象类型，
        * 因为在这种情况下，JSON.parseObject() 无法推断出要反序列化的类型。
        * */
        String json = stringRedisTemplate.opsForValue().get(Key);
        list= JSON.parseObject(json, new TypeReference<List<DishDto>>(){});

        //如果redis存在，则直接返回
         if(list!=null){
             return  R.success(list);
         }
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId,categoryId);
        if (status != null) {
            wrapper.eq(Dish::getStatus,status);
        }
        //获取该种类的所有菜品
        List<Dish> list2 = dishService.list(wrapper);
        for (Dish dish : list2) {
            DishDto dto = new DishDto();
            //拷贝值
            BeanUtils.copyProperties(dish,dto);
            Long id = dish.getId();
            LambdaQueryWrapper<DishFlavor> wrapper1 = new LambdaQueryWrapper<>();
            //获取该菜品的所有口味
            wrapper1.eq(DishFlavor::getDishId,id);
            List<DishFlavor> list3 = dishFlavorService.list(wrapper1);
            dto.setFlavors(list3);
            list.add(dto);
        }

        //如果不存在的情况，将查询出的菜品数据缓存到redis
        stringRedisTemplate.opsForValue().set(Key,JSON.toJSONString(list),60, TimeUnit.MINUTES);

        return  R.success(list);
    }



}