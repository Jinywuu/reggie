package com.wjy.service.imp;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wjy.entity.Category;
import com.wjy.mapper.CategoryMapper;
import com.wjy.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImp extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}
