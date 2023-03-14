package com.wjy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjy.common.R;
import com.wjy.entity.Employee;
import com.wjy.service.EmployeeService;
import com.wjy.service.imp.EmployeeServiceImp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    //RequestBody的作用是将前端传来的json格式的数据转为自己定义好的javabean对象，
    //前提是前端的Key需要与封装对象的属性相同，不然无法封装成功

    //。客户端浏览器发出的请求被封装成为一个HttpServletRequest对象。
    // 对象包含了客户端请求信息包括请求的地址，请求的参数，提交的数据，上传的文件客户端的ip甚至客户端操作系统都包含在其内
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码password进行MD5加密处理
        String password=employee.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据用户名查数据库
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        //访问哪个字段，就直接访问他在我们实体类中所对应的属性（Employee::getUsername）
        wrapper.eq(Employee::getUsername,employee.getUsername());//等值查询
        Employee emp = employeeService.getOne(wrapper);//用户名设置了唯一性，所以可以getone
        //3.如果没有查到返回失败登录的结果
        if(emp==null){
            return R.error("登陆失败");
        }
        //4.密码比对
        if(!emp.getPassword().equals(password)){
            return R.error("登陆失败");
        }
        //5.查看状态
        if(emp.getStatus()==0){
            return R.error("账号已经禁用");
        }
        //6.登陆成功，id存在session，返回成功的结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);

    }
    //退出功能
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理session中保存的员工信息
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpServletRequest request){

        log.info("新增员工信息{}",employee.toString());
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setCreateUser((Long) request.getSession().getAttribute("employee"));
//        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        employeeService.save(employee);
        return  R.success("新增员工成功");
    }
    //员工信息的分页查询
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page page1 = new Page(page, pageSize);
        //构造条件过滤器，判断name是否有值
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper();
        //模糊查询
        wrapper.like(StringUtils.isNotBlank(name),Employee::getName,name);
        //添加排序条件
        wrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(page1,wrapper);

        return R.success(page1);
    }
    //更改用户状态，或者更改用户信息用的时同一个接口
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());

//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }
    //修改员工时的页面显示
    @GetMapping("{id}")
    public R<Employee> update(@PathVariable(name="id",required = true) Long id){
        log.info("要修改的员工id:{}",id);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getId,id);
//        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
//        wrapper.eq(,id);
        Employee employee = employeeService.getOne(wrapper);
        return  R.success(employee);
    }

}
