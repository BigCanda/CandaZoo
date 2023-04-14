package com.newcoder.community.util;

import com.newcoder.community.entity.User;
import org.springframework.stereotype.Component;

/*
23.3.9
容器，持有用户信息，用于代替session对象，线程隔离
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();
    public void setUser (User user) {
        users.set(user);
    }
    public User getUser () {
        return users.get();
    }
    public void clear () {
        users.remove();
    }
}
