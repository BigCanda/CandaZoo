package com.newcoder.community;

import com.newcoder.community.services.AlphaServices;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SpringbootApplication.class)
public class TransactionTest {
    @Autowired
    private AlphaServices alphaServices;
    @Test
    public void testSave1() {
        Object obj = alphaServices.save1();
        System.out.println(obj);
    }

    @Test
    public void testSave2() {
        Object obj = alphaServices.save2();
        System.out.println(obj);
    }
}
