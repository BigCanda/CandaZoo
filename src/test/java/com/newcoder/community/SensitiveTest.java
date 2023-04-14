package com.newcoder.community;

import com.newcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SpringbootApplication.class)


public class SensitiveTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void setSensitiveFilter() {
        String text = "这里可以赌博,可以嫖娼,可以开票,可以哈哈哈,操你妈！傻逼，傻屌♥";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
