package com.newcoder.community;

import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.LoginTicketMapper;
import com.newcoder.community.dao.MessageMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.Message;
import com.newcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = SpringbootApplication.class)
public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("Atao2");
        user.setPassword("1234567");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);
        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "atao6666");
        System.out.println(rows);
    }

    @Test
    public void commentPostSelectTest() {
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);

        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost post : list) {
            System.out.println(post);
        }
    }

    @Test
    public void testLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);
        loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testCommonInsertTest() {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setStatus(0);
        discussPost.setUserId(101);
        discussPost.setContent("b");
        discussPost.setTitle("sb");
        discussPost.setCommentCount(10);
        discussPost.setCreateTime(new Date());
        discussPost.setType(0);
        discussPost.setScore(10);
        discussPostMapper.insertDiscussPost(discussPost);
    }

    @Test
    public void testSelectLetters() {
        List<Message> list = messageMapper.selectConversations(111, 0,20);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
            System.out.println(count);

        list = messageMapper.selectLetters("111_112",0,10);
        for (Message message : list) {
            System.out.println(message);
         }
        count =messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count =messageMapper.selectUnreadLetterCount(131,"111_131");
        System.out.println(count);

    }

    @Test
    public void testUpdate() {
        int id = 316;
        discussPostMapper.updateDiscussPostStatus(id, 0);
    }

    @Test
    public void testSelectAllUser() {
        List<User> users= userMapper.selectUsers(0 , 0);
        for (User user : users) {
            System.out.println(user);

        }
        try {
            Thread.sleep(20 * 1000);
        } catch (Exception e) {

        }
    }
}