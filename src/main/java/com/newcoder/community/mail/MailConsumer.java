package com.newcoder.community.mail;

import com.alibaba.fastjson.JSONObject;
import com.newcoder.community.entity.Event;
import com.newcoder.community.entity.Mail;
import com.newcoder.community.event.EventConsumer;
import com.newcoder.community.util.MailClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MailConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MailConsumer.class);

    @Autowired
    private MailClient mailClient;

    @KafkaListener(topics = "mail")
    public void handleSendMail(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空!");
            return;
        }

        Mail mail = JSONObject.parseObject(record.value().toString(), Mail.class);
        if (mail == null) {
            logger.error("消息格式错误!");
            return;
        }
        mail.setSubject(mail.getSubject());
        mail.setContent(mail.getContent());
        mail.setTo(mail.getTo());

        mailClient.sendMail(mail);

    }
}
