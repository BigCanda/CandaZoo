package com.newcoder.community.services;

import com.newcoder.community.dao.DiscussMapper;
import com.newcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussMapper discussMapper;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit){
        return discussMapper.selectDiscussPosts(userId,offset,limit);
    }
    public int findDiscussPostRows(int userId){
        return discussMapper.selectDiscussPostRows(userId);
    }


}
