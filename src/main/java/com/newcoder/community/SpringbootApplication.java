package com.newcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication标注了这个类是一个spring boot的应用
@SpringBootApplication
public class SpringbootApplication{
    //Spring的一个组件
    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }


}
