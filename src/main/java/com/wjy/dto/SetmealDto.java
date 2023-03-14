package com.wjy.dto;

import com.wjy.entity.Setmeal;
import com.wjy.entity.SetmealDish;
import lombok.Data;
import java.util.List;
//避免创建过多的表
@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
