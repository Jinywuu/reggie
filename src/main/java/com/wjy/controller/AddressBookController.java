package com.wjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjy.common.R;
import com.wjy.entity.AddressBook;
import com.wjy.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import com.wjy.common.BaseContext;
import java.util.List;

/**
 * 地址簿管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getID());
        log.info("addressBook:{}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook){
    log.info(addressBook.toString());
        Long id = addressBook.getId();
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        //先使用userid，让该用户所有的地址都设置为不默认
        //再使用地址ID，让某个地址变成默认地址
        wrapper.eq(AddressBook::getUserId,BaseContext.getID());
        wrapper.set(AddressBook::getIsDefault,0);
        addressBookService.update(wrapper);
        LambdaUpdateWrapper<AddressBook> wrapper2 = new LambdaUpdateWrapper<>();
        wrapper2.eq(AddressBook::getId,id).set(AddressBook::getIsDefault,1);
        addressBookService.update(wrapper2);
        return R.success("设置成功");
    }





















//    @PutMapping("default")
//    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
//        log.info("addressBook:{}", addressBook);
//        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
//        wrapper.eq(AddressBook::getUserId, BaseContext.getID());
//        wrapper.set(AddressBook::getIsDefault, 0);
//        //SQL:update address_book set is_default = 0 where user_id = ?
//        addressBookService.update(wrapper);
//
//        addressBook.setIsDefault(1);
//        //SQL:update address_book set is_default = 1 where id = ?
//        addressBookService.updateById(addressBook);
//        return R.success(addressBook);
//    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("没有找到该对象");
        }
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    public R<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getID());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if (null == addressBook) {
            return R.error("没有找到该对象");
        } else {
            return R.success(addressBook);
        }
    }

    /**
     * 查询指定用户的全部地址
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getID()   );
        log.info("addressBook:{}", addressBook);

        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        //SQL:select * from address_book where user_id = ? order by update_time desc
        return R.success(addressBookService.list(queryWrapper));
    }
}
