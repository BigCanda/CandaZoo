package com.newcoder.community.controller.adivise;

import com.newcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;


@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvise {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvise.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception{
        logger.error("服务器异常: ", e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
        // 异步请求数据
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            httpServletResponse.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常!"));
        } else {
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/error");
        }
    }
}
