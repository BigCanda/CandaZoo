package com.newcoder.community;

import com.newcoder.community.dao.AlphaDao;
import com.newcoder.community.services.AlphaServices;
import com.newcoder.community.services.UserService;
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
import java.util.*;
import java.util.concurrent.ConcurrentMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SpringbootApplication.class)
public class SpringbootApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired//自动装配去IoC里寻找依赖并返回
    private AlphaDao alphaDao;
    @Autowired
    private AlphaServices alphaServices;
    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Autowired
    private UserService userService;

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


    @Test
    public void testDI(){//依赖注入测试
        System.out.println(alphaDao);
        System.out.println(alphaServices);
        System.out.println(simpleDateFormat);
    }

    @Test
    public void loginTest() {
        System.out.println(userService.login("BigCanda2","9ce50633005800df90b4e05ef1c0015ba6bf6",1000));

    }

    @Test
    public void rememberMeTest () {
        String a = "abc dwd";
        System.out.println(a.indexOf("dwd"));
        System.out.println(a.substring(a.indexOf("dwd")));
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> map = new Hashtable<>();
        Map<String, Object> map1 = new ConcurrentMap<String, Object>() {
            @Override
            public Object putIfAbsent(String key, Object value) {
                return null;
            }

            @Override
            public boolean remove(Object key, Object value) {
                return false;
            }

            @Override
            public boolean replace(String key, Object oldValue, Object newValue) {
                return false;
            }

            @Override
            public Object replace(String key, Object value) {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @Override
            public Object get(Object key) {
                return null;
            }

            @Override
            public Object put(String key, Object value) {
                return null;
            }

            @Override
            public Object remove(Object key) {
                return null;
            }

            @Override
            public void putAll(Map<? extends String, ?> m) {

            }

            @Override
            public void clear() {

            }

            @Override
            public Set<String> keySet() {
                return null;
            }

            @Override
            public Collection<Object> values() {
                return null;
            }

            @Override
            public Set<Entry<String, Object>> entrySet() {
                return null;
            }
        };
        Map<String, Object> map2 = new HashMap<>();
        map2 = Collections.synchronizedMap(map2);
    }

}
