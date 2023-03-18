package com.wjy.dto;

import com.wjy.entity.Setmeal;
import com.wjy.entity.SetmealDish;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
//避免创建过多的表
@Data
public class SetmealDto extends Setmeal implements Serializable {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
