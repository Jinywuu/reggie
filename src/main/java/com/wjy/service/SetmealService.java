package com.wjy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wjy.common.R;
import com.wjy.dto.DishDto;
import com.wjy.dto.SetmealDto;
import com.wjy.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);
    //修改套餐时的数据回显
    public SetmealDto getByIdWithSetmealDish(Long id);
    //修改套餐
    public void updateSetmealDto(SetmealDto setmealDto);
    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    public R<String> removeWithDish(List<Long> ids);
}
