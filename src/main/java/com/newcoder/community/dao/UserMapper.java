package com.newcoder.community.dao;

import com.newcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper//或者@Repositiry
public interface UserMapper {
    User selectById(int id);
    List<User> selectUsers(int offset, int limit);
    User selectByName(String username);
    User selectByEmail(String email);
    int selectUserCount();
    int insertUser(User user);
    int updateStatus(int id,int status);
    int updateHeader(int id, String headerUrl);
    int updatePassword(int id,String password);


}
