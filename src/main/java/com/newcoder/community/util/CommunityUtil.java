package com.newcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Order;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


// 23.3.8
public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // MD5加密
    // 只能加密，不能解密，加密值固定 hello->abc123def456
    // 加上salt(3e4a8),hello->abc123def4563e4a8

    public static String md5(String key) {
        if(StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes(StandardCharsets.UTF_8));
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);

        if (map != null) {
            for(String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg,null);
    }
    public static String getJSONString(int code) {
        return getJSONString(code,null,null);
    }

    public static Sort getSearchSort() {

        List properties = new ArrayList();

        properties.add(new Order(Sort.Direction.DESC, "type"));
        properties.add(new Order(Sort.Direction.DESC, "score"));
        properties.add(new Order(Sort.Direction.DESC, "createTime"));

        return Sort.by(properties);
    }

    public static String HighlightString(String content, String keyword) {
        if (content.contains(keyword)) {
            int indexOfKeyword = content.indexOf(keyword);
            int length = content.length();
            String prefix = null;

            if (indexOfKeyword == 0) {
                String content2 = content.substring(indexOfKeyword + keyword.length());
                content = "<em>" + keyword + "</em>" + content2;
            } else if (indexOfKeyword == length-1) {
                String content1 = content.substring(0, indexOfKeyword);
                content = content1 + "<em>" + keyword + "</em>" ;
            } else {
                prefix = "...";
                String content1 = content.substring(0, indexOfKeyword);
                String content2 = content.substring(indexOfKeyword + keyword.length());
                content = content1 + "<em>" + keyword + "</em>" + content2;
            }

            indexOfKeyword = content.indexOf("<em>" + keyword + "</em>");

            if (content.length() > 60) {
                content = prefix + content.substring(indexOfKeyword);
            }
        }
        return content;
    }

}
