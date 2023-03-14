package com.wjy.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wjy.entity.OrderDetail;
import com.wjy.mapper.OrderDetailMapper;
import com.wjy.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}