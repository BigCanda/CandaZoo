package com.newcoder.community.dao;

import com.newcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface DiscussMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    //userId用来拼接个人主页
    // 动态条件且变量单一就一定要用@Param取别名
    int selectDiscussPostRows(@Param("userId") int userId);


}
