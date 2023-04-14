package com.newcoder.community.services;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.Mail;
import com.newcoder.community.entity.User;
import com.newcoder.community.mail.MailProducer;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailProducer mailProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }
    public User findUserByName(String username){

        return userMapper.selectByName(username);
    }
    public Map<String,Object> register(User user) {
        Map<String,Object> map = new HashMap<>();

        // 判断空值，处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        if (StringUtils.isBlank(user.getUsername())) {

            map.put("usernameMsg","用户名不能为空!");
            return map;
        }

        if (user.getUsername().length() < 4) {

            map.put("usernameMsg","用户名长度至少为4!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {

            map.put("passwordMsg","密码不能为空!");
            return map;
        }

        if (user.getPassword().length() < 10) {

            map.put("passwordMsg","密码长度至少为10!");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {

            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
        if (user.getEmail().length() <= 11) {

            map.put("emailMsg","邮箱格式错误!");
            return map;
        }


        // 验证账号
        User u = userMapper.selectByName(user.getUsername());

        if (u != null) {

            map.put("usernameMsg","该账号已存在");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());

        if (u != null) {
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }

        // 注册用户
        // 只取5位
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        // 密码设置为原密码+salt
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);

        String activationCode = CommunityUtil.generateUUID();
        user.setActivationCode(activationCode);
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);// 已经自动生成id并且回填了，详见配置文件

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());

        //http:localhost:8080/community/activation/${userid}/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + activationCode;
        context.setVariable("url",url);

        String content = templateEngine.process("mail/activation",context);
        Mail mail = new Mail();
        mail.setTo(user.getEmail());
        mail.setContent(content);
        mail.setSubject("激活牛客网账号");
        try {
            mailProducer.fireMail(mail);
        } catch (Exception e) {
            map.put("emailMsg", "邮箱格式错误!");
            return map;
        }

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if(user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId,1);
            clearCache(userId);

            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login (String username, String password, int expiredSecond) {

        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg","密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg","该账号不存在!");
            return map;
        }
        // 验证激活状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg","该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg","密码错误!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSecond * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket",loginTicket.getTicket());

        return map;
    }
    public void logout (String ticket) {
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket (String ticket) {

//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public Map<String,Object> modifyPassword(String oldPassword, String newPassword, String confirmPassword, String ticket) {
        Map<String, Object> map =new HashMap<>();

        if (oldPassword == null) {
            map.put("oldPasswordMsg","原密码不能为空!");
            return map;
        }
        if (newPassword == null) {
            map.put("newPasswordMsg","新密码不能为空!");
            return map;
        }

        if (newPassword.length() < 10) {
            map.put("newPasswordMsg","新密码长度至少为10!");
            return map;
        }

        if (confirmPassword == null) {
            map.put("confirmPasswordMsg","重复密码不能为空!");
            return map;
        }

        if (!newPassword.equals(confirmPassword)) {
            map.put("confirmPasswordMsg","两次输入密码不一致!");
            return map;
        }



//        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        LoginTicket loginTicket = findLoginTicket(ticket);

        int userId = loginTicket.getUserId();
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());

        if (!Objects.equals(oldPassword, user.getPassword())) {
            map.put("oldPasswordMsg","原密码错误!");
            return map;
        }

        if (CommunityUtil.md5(newPassword + user.getSalt()).equals(oldPassword)) {
            map.put("newPasswordMsg","新密码不能与原密码相同!");
            return map;
        }

        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(user.getId(), newPassword);

        return map;
    }

    public int updateHeaderUrl (int userId, String headerUrl) {

//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    // 1.优先从缓存中取数值

    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    // 2.取不到初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据发送变化就删除缓存
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Map<String, Object> getCode(String email) {
        Map<String,Object> map = new HashMap<>();


        if (StringUtils.isBlank(email)) {

            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
        if (email.length() <= 11) {

            map.put("emailMsg","邮箱格式错误!");
            return map;
        }

        User user = userMapper.selectByEmail(email);

        if (user == null) {
            map.put("emailMsg","该邮箱未注册!");
            return map;
        }
        if (user.getStatus() == 0){
            map.put("emailMsg","该邮箱还未激活!");
            return map;
        }

        String code = CommunityUtil.generateUUID().substring(0, 6);
        map.put("code", code);
        // 激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());

        context.setVariable("code",code);

        String content = templateEngine.process("mail/forget",context);

        Mail mail = new Mail();
        mail.setTo(user.getEmail());
        mail.setContent(content);
        mail.setSubject("修改牛客网密码");

        try {
            mailProducer.fireMail(mail);
        } catch (Exception e) {
            map.put("emailMsg", "发送失败,请稍后再试!");
            return map;
        }
        map.put("expirationTime", LocalDateTime.now().plusMinutes(5));
        map.put("cd", LocalDateTime.now().plusMinutes(1));
        return map;
    }

    public Map<String, Object> resetPassword(String password, String code) {
        Map<String, Object> map = new HashMap<>();

        if (code.isEmpty()) {
            map.put("codeMsg", "验证码不能为空!");
            return map;
        }
        if (password.isEmpty()) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        if (password.length() < 10) {
            map.put("passwordMsg", "密码长度不能小于10位!");
            return map;
        }

        return map;
    }

    public void updatePasswordByEmail(String email, String password) {
        User user = userMapper.selectByEmail(email);
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);
    }

    public User selectUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    public Collection<? extends GrantedAuthority> grantedAuthorities(int userId) {

        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                    switch (user.getType()) {
                        case 1:
                            return AUTHORITY_ADMIN;
                        case 2:
                            return AUTHORITY_MODERATOR;
                        default:
                            return AUTHORITY_USER;
                    }
            }
        });
        return list;
    }
}
