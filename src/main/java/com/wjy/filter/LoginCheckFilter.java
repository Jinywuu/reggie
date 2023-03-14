package com.wjy.filter;

import com.alibaba.fastjson.JSON;
import com.wjy.common.BaseContext;
import com.wjy.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//检查用户是否已经登录
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
@Component
public class LoginCheckFilter implements Filter {
    public static  final   AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取本次请求的uri(在主机存放资源的目录)
        String uri = request.getRequestURI();
        log.info("拦截到请求：{}",uri);
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };
        boolean b = check(urls, uri);
        if (b) {
            log.info("本次请求不需要处理{}",uri);
            filterChain.doFilter(request, response);
            return;
        }
        //判断登录状态，如果已经登录则放行
        if (request.getSession().getAttribute("employee") != null) {

            log.info("用户已经登录id为：{}",request.getSession().getAttribute("employee"));
            //把公共字段的值放在ThreadLocal里面
            BaseContext.setID((Long) request.getSession().getAttribute("employee"));

            filterChain.doFilter(request, response);
            return;
        }

        //4-2、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setID(userId);

            filterChain.doFilter(request,response);
            return;
        }





        //如果未登录则返回未登录结果，通过输出流的方式返回
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }
     //检测本次请求是否需要放行
     public boolean check(String [] urls,String uri){

         for (String url : urls) {
             boolean b = PATH_MATCHER.match(url, uri);
             if(b){ return true;}
         }
         return false;
     }
}
