package com.newcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.newcoder.community.entity.User;
import com.newcoder.community.services.SearchService;
import com.newcoder.community.services.UserService;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.newcoder.community.util.CommunityConstant.*;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SearchService searchService;

    @Value("${server.servlet.context-path}")
    private String contextPath;
    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage() {
        //!
        return "site/register";
    }
    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user, String code/*, HttpSession httpSession*/,
                           @CookieValue( value = "kaptchaOwner" ,required = false) String kaptchaOwner) {
        String kaptcha;
        if (StringUtils.isNotEmpty(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        } else {
            model.addAttribute ("codeMsg","验证码输入超时,请点击击图片刷新验证码！");
            return "site/register";
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute ("codeMsg","验证码不正确,请点击击图片刷新验证码！");
            return "site/register";
        }

        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()) {
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活!(10分钟内有效)如果没有找到激活邮件，请检查回收站");
            searchService.saveUser(user);
            model.addAttribute("target","/index");
            return "site/operate-result";
        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
        }
        return "site/register";
    }

    // 23.3.8
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code) {
        int result = userService.activation(userId,code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg","激活成功,您的账号已经可以正常使用!");
            model.addAttribute("target","/login");
        } else if(result == ACTIVATION_REPEAT) {
            model.addAttribute("msg","无效操作,该账号已经激活!");
            model.addAttribute("target","/index");
        } else {
            model.addAttribute("msg","激活失败,激活码不正确!");
            model.addAttribute("target","/index");
        }
        return "site/operate-result";
    }

    // 23.3.8
    // 登录页面跳转
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage() {
        return "site/login";
    }

    // 23.3.8
    // 验证码状态判断
    @RequestMapping(path="/login", method = RequestMethod.POST)
    public String login (String username, String password,
                         String code, boolean rememberMe, Model model,/*, HttpSession httpSession*/
                         HttpServletResponse httpServletResponse
                        ,@CookieValue( value = "kaptchaOwner" ,required = false) String kaptchaOwner) {

//        String kaptcha = (String) httpSession.getAttribute("kaptcha");
        String kaptcha;
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;

        if (StringUtils.isNotEmpty(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        } else {
            model.addAttribute ("codeMsg","验证码输入超时,请重新输入验证码！");
            return "site/login";
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute ("codeMsg","验证码不正确！");
            return "site/login";
        }

        // 账号密码检查
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            httpServletResponse.addCookie(cookie);
            return "redirect:/index";

        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "site/login";
        }
    }

    // 23.3.8
    // 获取验证码图片
    @RequestMapping(path="/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response /*, HttpSession session*/){
        //生成验证码，需要先注入Bean,生成一个四位的字符串
        String text = kaptchaProducer.createText();

        BufferedImage image = kaptchaProducer.createImage(text); //利用字符串去生成图片
        //将验证码存入session
        //session.setAttribute("kaptcha", text);

        // 验证码归属
        String kaptchaOwner = CommunityUtil.generateUUID();

        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge (60);
        cookie.setPath (contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);
        //将图片输出给浏览器，人工输出(也就是直接输出)
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        }catch (IOException e){
            e.printStackTrace();
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }
    @RequestMapping(path="/logout", method = RequestMethod.GET)
    public String logout (@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:login";
    }
}
