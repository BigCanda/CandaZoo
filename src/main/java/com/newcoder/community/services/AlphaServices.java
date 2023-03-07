package com.newcoder.community.services;

import com.newcoder.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class AlphaServices {
    @Autowired
    private AlphaDao alphaDao;
    @PostConstruct//在构造之后建立
    public void init(){
        System.out.println("AlphaServices初始化完成");
    }
    public String find(){//实现service调用Dao
        return alphaDao.select();
    }
    @PreDestroy
    public void destroy(){
        System.out.println("AlphaServices成功销毁");
    }

}
