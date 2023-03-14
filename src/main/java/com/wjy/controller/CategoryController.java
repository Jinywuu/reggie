package com.wjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjy.common.R;
import com.wjy.entity.Category;
import com.wjy.mapper.CategoryMapper;
import com.wjy.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    //分类管理的分页查询
    @GetMapping("/page")
    public R<Page>  pageshow(int page,int pageSize){
//     //构造分页构造器
//        log.info("page为：{} pagesize为：{}",page,pageSize);
//        Page<Category> page1 = new Page<>(page,pageSize);
//        categoryService.page(page1,null);
//        return  R.success(page1);

        //分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);

        //分页查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
    @PostMapping
    public R<String> save(@RequestBody Category category){
            log.info("category:{}",category.toString());
            categoryService.save(category);
        return R.success("新增分类成功");
    }
    @PutMapping
    public R<String> update(@RequestBody Category category){
//        log.info("修改后的category：{}",category.toString());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getId,category.getId());
        categoryService.update(category,wrapper);
        return R.success("修改成功");
    }
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("要删除的id为{}",ids);
        categoryService.removeById(ids);
        return R.success("删除成功");
    }
    @GetMapping("/list")
    public R<List<Category>> getCategory(Category category){
//      log.info("请求获取的类型为：{}",type);
//        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Category::getType,type);
//        List<Category> list = categoryService.list(wrapper);
//        return R.success(list);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}
