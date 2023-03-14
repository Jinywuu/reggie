package com.wjy.controller;
//购物车

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wjy.common.BaseContext;
import com.wjy.common.R;
import com.wjy.entity.ShoppingCart;
import com.wjy.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public  class ShoppingCartController{
    @Autowired
    ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> addcar(@RequestBody ShoppingCart shoppingCart, HttpServletRequest request){
        Long userid = (Long) request.getSession().getAttribute("user");
        shoppingCart.setUserId(userid);
        Long dishId = shoppingCart.getDishId();
        //查询当前菜品或者套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        //根据用户查到对应的菜品
        wrapper.eq(ShoppingCart::getUserId,userid);
        if(dishId!=null){
            //如果是菜品，则加上菜品的查询条件
            wrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //如果是套餐，则加上套餐的查询条件
            wrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //根据用户id，菜品/套餐id查询出来的一条shoppingCar
        ShoppingCart cart = shoppingCartService.getOne(wrapper);
        //如果商品存在
        if(cart!=null){
            cart.setNumber(cart.getNumber()+1);
            shoppingCartService.updateById(cart);
            return R.success(cart);
        }else {
            //如果不存在
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            return R.success(shoppingCart);
        }
    }


    //查看购物车
    @GetMapping("/list")
    public R<List<ShoppingCart>> getlist(){
        log.info("查看购物车。。。。。");
        //不同的用户只能看自己的购物信息
        Long userid = BaseContext.getID();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userid).orderByAsc();
        List<ShoppingCart> list = shoppingCartService.list(wrapper);
        return R.success(list);
    }
    //清空购物车
    @DeleteMapping("clean")
    public R<String> delete(){
        log.info("清空购物车。。。。。");
        //确定用户
        Long userid = BaseContext.getID();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userid);
        shoppingCartService.remove(wrapper);
        return R.success("清空成功");
    }

    ////购物车中修改商品(减少)
    @PostMapping("/sub")
    public R<ShoppingCart> update(@RequestBody Map map){
        log.info("修改购物车。。。。。。");
        //确定用户
        Long userid = BaseContext.getID();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userid);
        if(map.get("dishId")!=null){
            wrapper.eq(ShoppingCart::getDishId,map.get("dishId"));
            ShoppingCart cart = shoppingCartService.getOne(wrapper);
            cart.setNumber(cart.getNumber()-1);
            if(cart.getNumber()==0){
                shoppingCartService.removeById(cart);
            }
            shoppingCartService.updateById(cart);
            return R.success(cart);
        }else {
            wrapper.eq(ShoppingCart::getSetmealId,map.get("setmealId"));
            ShoppingCart cart = shoppingCartService.getOne(wrapper);

            cart.setNumber(cart.getNumber()-1);

            if(cart.getNumber()==0){
                shoppingCartService.removeById(cart);
            }
            shoppingCartService.updateById(cart);
            return R.success(cart);
        }
    }




}