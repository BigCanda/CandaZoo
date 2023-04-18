package com.newcoder.community.mail;

import com.alibaba.fastjson.JSONObject;
import com.newcoder.community.entity.Mail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MailProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void fireMail(Mail mail) {
        kafkaTemplate.send("mail", JSONObject.toJSONString(mail));
    }
}
