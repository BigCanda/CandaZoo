package com.newcoder.community.controller.interceptor;

import com.newcoder.community.entity.User;
import com.newcoder.community.services.MessageService;
import com.newcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import reactor.util.annotation.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        User user = hostHolder.getUser();

        if (user != null && modelAndView != null) {
            int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
            int unreadLetterCount =messageService.findUnreadLetterCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount", unreadNoticeCount + unreadLetterCount);

        }
    }
}
