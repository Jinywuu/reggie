package com.wjy.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wjy.entity.ShoppingCart;
import com.wjy.mapper.ShoppingCartMapper;
import com.wjy.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

}
