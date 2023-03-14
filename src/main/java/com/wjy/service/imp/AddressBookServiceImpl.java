package com.wjy.service.imp;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wjy.entity.AddressBook;
import com.wjy.mapper.AddressBookMapper;
import com.wjy.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
