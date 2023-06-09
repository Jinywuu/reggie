package com.wjy.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wjy.entity.SetmealDish;
import com.wjy.mapper.SetmealDishMapper;
import com.wjy.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
