package com.newcoder.community.dao;

import com.newcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired}) "
    })

    // 配置自动输入id
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket (LoginTicket loginTicket);
    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket = #{ticket} "
    })
    LoginTicket selectByTicket(String ticket);
    @Update({
            "<script>",
            "update login_ticket set status = #{status} where ticket = #{ticket} ",
            "<if test = \"ticket != null\">",
            "and 1 = 1",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);
}
