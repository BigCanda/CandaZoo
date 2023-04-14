package com.newcoder.community.dao;

import com.newcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表,针对每个会话只返回一条最新私信
    List<Message> selectConversations(int userId, int offset, int limit);

    int selectConversationCount(int userId);

    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询私信总数
    int selectLetterCount(String conversationId);

    // 查询未读消息数量
    int selectUnreadLetterCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    // 修改消息状态
    int updateStatus(List<Integer> ids,int status);

    // 查询某主题下最新通知
    Message selectLatestNotice(int userId, String topic);
    // 查询某主题下通知数量
    int selectNoticeCount(int userId, String topic);
    // 查询未读通知数量
    int selectUnreadNoticeCount(int userId, String topic);

    // 查询某个主题下的通知
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
