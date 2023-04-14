package com.newcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.newcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    // 处理事件主题是xxx,然后内容是什么,
    public void fireEvent(Event event) {
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
