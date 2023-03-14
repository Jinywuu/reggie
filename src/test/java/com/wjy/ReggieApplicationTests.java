package com.wjy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

@SpringBootTest
class ReggieApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void boxTest(){
        Random random = new Random();
        int[] arr = new int[30];
        for (int i = 0; i < arr.length; i++) {
                arr[i]=10+random.nextInt(81);
        }
        Arrays.sort(arr);
        for (int i = 0; i < arr.length;i+=5) {
              for(int j=i+1;j<i+4;j++){
                  if ((arr[j]-arr[i])<(arr[i+4]-arr[j])){
                      arr[j]=arr[i];
                  }
                  else{
                      arr[j]=arr[i+4];
                  }
              }
        }
        System.out.println(arr.toString());
    }
}
