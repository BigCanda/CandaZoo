package com.newcoder.community.controller;

import com.newcoder.community.entity.*;
import com.newcoder.community.services.FollowService;
import com.newcoder.community.services.LikeService;
import com.newcoder.community.services.SearchService;
import com.newcoder.community.services.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private SearchService searchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @RequestMapping(path = "/searchPost", method = RequestMethod.GET)
    public String searchDiscussPost(String keyword, Model model, Page page) {

        Sort sort = CommunityUtil.getPostSearchSort();


        Pageable pageable = PageRequest.of(page.getCurrent() - 1, page.getLimit(),sort);
        page.setPath("/searchPost?keyword=" + keyword);

        SearchPostResult searchPostResult = searchService.searchDiscussPost(keyword, pageable);

        long rows = searchPostResult.getRows();
        page.setRows((int) rows);

        List<DiscussPost> list = searchPostResult.getPosts();
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost discussPost : list) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",discussPost);

                User user = userService.findUserById(discussPost.getUserId());
                map.put("user",user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);
        model.addAttribute("rows",rows);

        return "site/search-post";
    }
    @RequestMapping(path = "/searchUser", method = RequestMethod.GET)
    public String searchUser(String keyword, Model model, Page page) {

        User loginUser = hostHolder.getUser();
        Sort sort = CommunityUtil.getUserSearchSort();
        Pageable pageable = PageRequest.of(page.getCurrent() - 1, page.getLimit(), sort);

        page.setPath("/searchUser?keyword=" + keyword);

        SearchUserResult searchUserResult = searchService.searchUser(keyword, pageable);

        long rows = searchUserResult.getRows();
        page.setRows((int) rows);

        List<User> list = searchUserResult.getUsers();
        List<Map<String,Object>> users = new ArrayList<>();
        if (list != null) {
            for (User user : list) {
                Map<String,Object> map = new HashMap<>();
                map.put("user",user);
                map.put("hasFollowed", hasFollowed(user.getId()));
                users.add(map);
            }
        }

        model.addAttribute("users",users);
        model.addAttribute("keyword",keyword);
        model.addAttribute("rows",rows);
        model.addAttribute("loginUser",loginUser);

        return "site/search-user";
    }

    private boolean hasFollowed(int userId) {
        if(hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}
