package com.wjy.common;
/*
* ThreadLocal:
* 每一次http请求有事一个线程来实现，ThreadLocal可以为每一个线程创建一个单独使用的变量，线程之间互相不影响
* */
public class BaseContext {
    private  static ThreadLocal<Long> threadLocal =new ThreadLocal<>();
    //作用范围时每个线程以内
    public static  void setID(Long id){
           threadLocal.set(id);
    }
    public static Long getID(){
      return threadLocal.get();
    }
}
