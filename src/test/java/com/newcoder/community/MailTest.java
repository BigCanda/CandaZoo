package com.newcoder.community;

import com.newcoder.community.entity.Mail;
import com.newcoder.community.mail.MailProducer;
import com.newcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SpringbootApplication.class)
public class MailTest {
    @Autowired
    private MailProducer mailProducer;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "sunday");

        String content = templateEngine.process("/mail/demo.html", context);
        Mail mail = new Mail();
        mail.setContent(content);
        mail.setTo("1799918862@qq.com");
        mail.setSubject("HTML！");
        System.out.println(content);
        mailProducer.fireMail(mail);
    }

    @Test
    public void testTextMail() {
        int i;
        for(i = 3; i > 1; i--) {
            try {
                Thread.sleep(1000);
                //mailClient.sendMail("3266035442@qq.com","你死定辣！","你的手机还有"+Integer.toString(i)+"秒爆炸");
                //mailClient.sendMail("1319390655@qq.com","GG！","你的手机还有"+Integer.toString(i)+"秒爆炸");
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }


}
