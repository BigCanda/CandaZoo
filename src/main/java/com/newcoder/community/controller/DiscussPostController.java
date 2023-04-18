package com.newcoder.community.controller;

import com.newcoder.community.entity.*;
import com.newcoder.community.event.EventProducer;
import com.newcoder.community.services.*;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant{

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private FollowService followService;


    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();

        if (user == null) {
            return CommunityUtil.getJSONString(403,"您还没有登录!");

        }

        if (content.isEmpty() || title.isEmpty()) {
            return CommunityUtil.getJSONString(1,"发布失败,内容与标题不能为空!");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());

        discussPostService.addDiscussPost(discussPost);
        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        List<Map<String, Object>> followers = followService.findFollower(user.getId(), 0,(int)followService.findFollowerCount(ENTITY_TYPE_USER, user.getId()));
        if (followers != null) {
            for (Map<String, Object> follower : followers) {
                User u = (User) follower.get("user");
                event = new Event()
                        .setTopic(TOPIC_PUSH)
                        .setUserId(hostHolder.getUser().getId())
                        .setEntityType(ENTITY_TYPE_USER)
                        .setEntityId(discussPost.getId())
                        .setEntityUserId(u.getId())
                        .setData("postId",discussPost.getId());
                eventProducer.fireEvent(event);
            }
        }
        // 报错将来统一处理
        return CommunityUtil.getJSONString(0,"发布成功!");
    }
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);

        if (post.getStatus() == 2 && hostHolder.getUser().getType() == 0) {
            return "error/404";
        }
        model.addAttribute("post",post);

        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        long likeCount = likeService.findEntityLikeCount (ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);

        model.addAttribute("loginUser",hostHolder.getUser());

        int likeStatus;
        // 如果用户没有登录,那么用户id就没有
        if (hostHolder.getUser() == null) {
            likeStatus = likeService.findEntityLikeStatus(0, ENTITY_TYPE_POST, discussPostId);
        } else {
            likeStatus = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        }

        model.addAttribute("likeStatus",likeStatus);
        // 查评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());

        // 评论：给贴子的评论
        // 回复：给评论的评论
        List<Comment> comments = commentService.findCommentByEntity(ENTITY_TYPE_POST,post.getId(),page.getOffset(),page.getLimit());
        // comment value object list,评论的VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();

        for (Comment comment : comments) {
            //一个评论的VO
            Map<String, Object> commentVo = new HashMap<>();
            // 放1个评论
            commentVo.put("comment", comment);
            // 放一个评论作者
            commentVo.put("user", userService.findUserById(comment.getUserId()));

            likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
            commentVo.put("likeCount", likeCount);

            // 如果用户没有登录,那么用户id就没有
            if (hostHolder.getUser() == null) {
                likeStatus = likeService.findEntityLikeStatus(0, ENTITY_TYPE_COMMENT, comment.getId());
            } else {
                likeStatus = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
            }
            commentVo.put("likeStatus", likeStatus);
            // 回复列表
            List<Comment> replyList = commentService.findCommentByEntity(
                    ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

            List<Map<String, Object>> replyVoList = new ArrayList<>();
            if (replyList != null) {
                for (Comment reply : replyList) {
                    Map<String, Object> replyVo = new HashMap<>();
                    replyVo.put("reply", reply);
                    replyVo.put("user", userService.findUserById(reply.getUserId()));

                    // 回复的目标
                    User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                    replyVo.put("target", target);

                    // 点赞数量
                    likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                    replyVo.put("likeCount", likeCount);

                    // 如果用户没有登录,那么用户id就没有
                    if (hostHolder.getUser() == null) {
                        likeStatus = likeService.findEntityLikeStatus(0, ENTITY_TYPE_COMMENT, comment.getId());
                    } else {
                        likeStatus = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                    }
                    replyVo.put("likeStatus", likeStatus);

                    replyVoList.add(replyVo);
                }
                commentVo.put("replies", replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
            }
            commentVoList.add(commentVo);
        }
        model.addAttribute("comments", commentVoList);
        return "site/discuss-detail";
    }
    @RequestMapping(path="/delete", method = RequestMethod.POST)
    @ResponseBody
    public String delete(int id) {

        if (discussPostService.updateDiscussPostStatus(id, DELETED_POST_STATUS) == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_DELETE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(id);
            eventProducer.fireEvent(event);

             return CommunityUtil.getJSONString(0,"删除成功!");
        }
        return CommunityUtil.getJSONString(1,"删除失败!");
    }

    // 置顶
    @RequestMapping(path="/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        if (discussPostService.updateDiscussPostType(id, TOP_POST_TYPE) == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_TOP)
                    .setEntityType(ENTITY_TYPE_USER)
                    .setEntityId(id)
                    .setEntityUserId(discussPostService.findDiscussPostById(id).getUserId())
                    .setData("postId", id);
            eventProducer.fireEvent(event);
            return CommunityUtil.getJSONString(0,"置顶成功!");
        }
        return CommunityUtil.getJSONString(1,"置顶失败!");
    }
    // 加精
    @RequestMapping(path="/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        if (discussPostService.updateDiscussPostStatus(id, WONDERFUL_POST_STATUS) == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_WONDERFUL)
                    .setEntityType(ENTITY_TYPE_USER)
                    .setEntityId(id)
                    .setEntityUserId(discussPostService.findDiscussPostById(id).getUserId())
                    .setData("postId", id);
            eventProducer.fireEvent(event);

            return CommunityUtil.getJSONString(0,"取消加精成功!");
        }
        return CommunityUtil.getJSONString(1,"取消加精失败!");
    }
    @RequestMapping(path="/unTop", method = RequestMethod.POST)
    @ResponseBody
    public String unTop(int id) {
        if (discussPostService.updateDiscussPostType(id, NORMAL_POST_TYPE) == 1) {
            return CommunityUtil.getJSONString(0,"取消置顶成功!");
        }
        return CommunityUtil.getJSONString(1,"取消置顶失败!");
    }


    @RequestMapping(path="/unWonderful", method = RequestMethod.POST)
    @ResponseBody
    public String unWonderful(int id) {
        if (discussPostService.updateDiscussPostStatus(id, NORMAL_POST_STATUS) == 1) {
            return CommunityUtil.getJSONString(0,"取消加精成功!");
        }
        return CommunityUtil.getJSONString(1,"取消加精失败!");
    }


}
