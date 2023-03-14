package com.wjy.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wjy.entity.User;
import com.wjy.mapper.UserMapper;
import com.wjy.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
