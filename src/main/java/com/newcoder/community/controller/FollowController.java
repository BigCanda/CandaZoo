package com.newcoder.community.controller;

import com.newcoder.community.entity.Event;
import com.newcoder.community.entity.Page;
import com.newcoder.community.entity.User;
import com.newcoder.community.event.EventProducer;
import com.newcoder.community.services.FollowService;
import com.newcoder.community.services.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        if (user.getId() == entityId) {
            return CommunityUtil.getJSONString(1,"不能关注自己!");
        } else {
            followService.follow(user.getId(),entityType,entityId);

            // 触发关注事件,发送系统通知
            Event event = new Event()
                    .setTopic(TOPIC_FOLLOW)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_USER)
                    .setEntityId(entityId)
                    .setEntityUserId(entityId);

            eventProducer.fireEvent(event);

            return CommunityUtil.getJSONString(0,"已关注!");
        }
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unFollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0,"已取消关注!");
    }

    @RequestMapping(path = "/user/followee/{userId}", method = RequestMethod.GET)
    public String getFolloweePage(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/user/followee/" + userId);
        page.setRows((int)followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowee(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "site/followee";
    }

    private boolean hasFollowed(int userId) {
        if(hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
    @RequestMapping(path = "/user/follower/{userId}", method = RequestMethod.GET)
    public String getFollowerPage(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/user/follower/" + userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followService.findFollower(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);

        return "site/follower";
    }
}
