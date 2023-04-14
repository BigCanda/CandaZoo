package com.newcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    public static String getValue (HttpServletRequest httpServletRequest, String name) {

        if (httpServletRequest == null || name == null) {
            throw new IllegalArgumentException("参数为空");
        }
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            // 挨个判断cookie名是否相等
            for (Cookie  cookie : cookies){
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
