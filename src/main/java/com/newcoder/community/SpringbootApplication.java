package com.newcoder.community;

import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.elasticsearch.DiscussPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

//@SpringBootApplication标注了这个类是一个spring boot的应用
@SpringBootApplication
public class SpringbootApplication{
    @PostConstruct
    public void init() {
        // 解决netty启动冲突的问题
        // Netty4Utils.setAvailableProcessor()

        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }
    //Spring的一个组件
    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }


}
