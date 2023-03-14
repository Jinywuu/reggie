package com.wjy.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
//处理加了这些注解的controller（aop）
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
//处理用户存在的添加异常
public class GlobalExceptionHandler {
    //处理唯一性字段重复出现的异常+
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> excepitions(SQLIntegrityConstraintViolationException ex){
            log.error(ex.getMessage());
            if(ex.getMessage().contains("Duplicate entry")){
                String[] split = ex.getMessage().split(" ");
                String msg = split[2] + "已存在";
                return R.error(msg);
            }
            return R.error("未知的错误");
      }
}
