package com.newcoder.community;

import com.newcoder.community.dao.AlphaDao;
import com.newcoder.community.services.AlphaServices;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SpringbootApplication.class)
public class SpringbootApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    @Test//测试注解
    public void testApplicationContext(){
        System.out.println(applicationContext);
        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao.select());//调用Dao的select()
    }

    @Test
    public void testBeanManagement(){//测试Bean管理
        AlphaServices alphaServices = applicationContext.getBean(AlphaServices.class);
        System.out.println(alphaServices);
    }

    @Test
    public void testBeanConfig(){//测试Bean设置，引入外来的Bean
        SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    @Autowired//自动装配去IoC里寻找依赖并返回
    private AlphaDao alphaDao;
    @Autowired
    private AlphaServices alphaServices;
    @Autowired
    private SimpleDateFormat simpleDateFormat;
    @Test
    public void testDI(){//依赖注入测试
        System.out.println(alphaDao);
        System.out.println(alphaServices);
        System.out.println(simpleDateFormat);
    }
}
