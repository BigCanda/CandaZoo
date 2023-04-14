package com.newcoder.community.controller;

import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.Page;
import com.newcoder.community.entity.User;
import com.newcoder.community.services.LikeService;
import com.newcoder.community.services.SearchService;
import com.newcoder.community.services.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
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

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Model model, Page page) {

        Sort sort = CommunityUtil.getSearchSort();


        Pageable pageable = PageRequest.of(page.getCurrent() - 1, page.getLimit(),sort);
        page.setPath("/search?keyword=" + keyword);

        long rows = searchService.search(keyword, pageable).getRows();
        page.setRows((int) rows);

        List<DiscussPost> list = searchService.search(keyword, pageable).getPosts();
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
        return "site/search";
    }

}
