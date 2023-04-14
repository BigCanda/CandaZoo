package com.newcoder.community.services;

import com.newcoder.community.dao.AlphaDao;
import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.User;
import com.newcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
public class AlphaServices {
    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @PostConstruct//在构造之后建立
    public void init() {
        //discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(0,0,10));
        System.out.println("AlphaServices初始化完成");
    }

    public String find() {//实现service调用Dao
        return alphaDao.select();
    }

    @PreDestroy
    public void destroy() {
        System.out.println("AlphaServices成功销毁");
    }

    @Autowired
    private UserMapper userMapper;

    // REQUIRED:支持当前事务（外部事务),如果外部事务不存在就创建外部事务
    // REQUIRES NEW: 创建一个新事务,并且创建一个新事务(外部事物)
    // NESTED 如果当前存在事务(外部事务),则嵌套在该事务中执行(独立的提交和回滚),否则和REQUIRES一样
    // 声明式事务
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        //新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道!");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);
        Integer.valueOf("abc");
        return "ok";
    }
    // 编程式事务
    public Object save2() {
        //transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setIsolationLevel(2);
        //transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setPropagationBehavior(0);
        return transactionTemplate.execute(new TransactionCallback<Object>() {

            @Override
            public Object doInTransaction(TransactionStatus status) {

                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);
                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("Hello");
                post.setContent("新人报道!");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);
                Integer.valueOf("abc");
                return "ok";
            }
        });
    }
}
