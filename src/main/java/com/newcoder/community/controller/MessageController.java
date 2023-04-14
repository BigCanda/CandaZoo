package com.newcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.newcoder.community.entity.Message;
import com.newcoder.community.entity.Page;
import com.newcoder.community.entity.User;
import com.newcoder.community.services.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    // 私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));//会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put ("conversation", message);
                map.put("fromUser",userService.findUserById(message.getFromId()).getUsername());
                map.put ("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put ("unreadCount", messageService.findUnreadLetterCount(user.getId(), message.getConversationId ()));
                int targetId = user.getId() == message.getFromId() ? message.getToId () : message.getFromId();
                map.put ("target", userService.findUserById (targetId));
                conversations.add(map);

            }
        }
        // 查询未读消息数量
        model.addAttribute("conversations",conversations);
        int unreadLetterCount = messageService.findUnreadLetterCount (user.getId(),null);
        model.addAttribute("unreadLetterCount",unreadLetterCount);
        model.addAttribute("fromUser",user.getUsername());

        int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("unreadNoticeCount", unreadNoticeCount);
        return "site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page, Model model) {
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                if (hostHolder.getUser().getId() == message.getFromId()) {
                    map.put("direction", SEND);
                } else {
                    map.put("direction", RECEIVE);
                }
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 设置目标
        model.addAttribute("target",getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "site/letter-detail";
    }

    // 获取目标用户的id
    private User getLetterTarget (String conversationId) {
        // 以_为分界分为若干个字符串
        String [] ids = conversationId.split("_");
        int id0= Integer.parseInt(ids[0]);
        int id1= Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if(letterList != null) {
            for(Message message : letterList) {
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String toName, String content) {
        User target = userService.findUserByName(toName);
        if(target == null) {
            return CommunityUtil.getJSONString(1,"目标用户不存在!");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());

        if (message.getToId() > message.getFromId()) {
            message.setConversationId(message.getFromId() +  "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId() );
        }
        message.setContent(content);
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();
        int count = 0;
        int unreadCount = 0;
        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVO = new HashMap<>();
        if(message != null) {
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);

            unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unreadCount", unreadCount);

        } else {
            messageVO.put("message", message);
        }
        model.addAttribute("commentNotice",messageVO);

        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();

        if(message != null) {
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);

            unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unreadCount", unreadCount);
        } else {
            messageVO.put("message", message);
        }
        model.addAttribute("likeNotice",messageVO);

        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();

        if(message != null) {
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));

            count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);

            unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unreadCount", unreadCount);
        } else {
            messageVO.put("message", message);
        }
        model.addAttribute("followNotice",messageVO);

        // 查询置顶通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_PUSH);
        messageVO = new HashMap<>();

        if(message != null) {
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            count = messageService.findNoticeCount(user.getId(), TOPIC_PUSH);
            messageVO.put("count", count);

            unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_PUSH);
            messageVO.put("unreadCount", unreadCount);
        } else {
            messageVO.put("message", message);
        }
        model.addAttribute("followArticleNotice",messageVO);

        // 查询加精通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_TOP);
        messageVO = new HashMap<>();

        if(message != null) {
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            count = messageService.findNoticeCount(user.getId(), TOPIC_TOP);
            messageVO.put("count", count);

            unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_TOP);
            messageVO.put("unreadCount", unreadCount);
        } else {
            messageVO.put("message", message);
        }
        model.addAttribute("topNotice",messageVO);

        message = messageService.findLatestNotice(user.getId(), TOPIC_WONDERFUL);
        messageVO = new HashMap<>();

        if(message != null) {
            messageVO.put("message", message);

            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            count = messageService.findNoticeCount(user.getId(), TOPIC_WONDERFUL);
            messageVO.put("count", count);

            unreadCount = messageService.findUnreadNoticeCount(user.getId(), TOPIC_WONDERFUL);
            messageVO.put("unreadCount", unreadCount);
        } else {
            messageVO.put("message", message);
        }
        model.addAttribute("wonderfulNotice",messageVO);

        // 查询未读消息数量
        int unreadLetterCount = messageService.findUnreadLetterCount(user.getId(), null);
        model.addAttribute("unreadLetterCount", unreadLetterCount);
        int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
        model.addAttribute("unreadNoticeCount", unreadNoticeCount);
        return "site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setPath("/notice/detail/" + topic);
        page.setLimit(5);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVOList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);

                // 通知内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVOList.add(map);
            }
        }
        model.addAttribute("notices", noticeVOList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "site/notice-detail";
    }
}
