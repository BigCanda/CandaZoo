package com.newcoder.community.controller;

import com.newcoder.community.annotation.LoginRequired;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.Page;
import com.newcoder.community.entity.User;
import com.newcoder.community.services.DiscussPostService;
import com.newcoder.community.services.FollowService;
import com.newcoder.community.services.LikeService;
import com.newcoder.community.services.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Controller
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private LoginController loginController;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @LoginRequired
    @RequestMapping(path = "/user/setting", method = RequestMethod.GET)
    public String getSettingPage () {
        return "site/setting";
    }
    @LoginRequired
    @RequestMapping(path = "/user/upload", method = RequestMethod.POST)
    public String uploadHeader (MultipartFile headerImage, Model model) {

        if(headerImage == null) {
            model.addAttribute("error", "你还未选择图片!");
            return "site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        // 取后缀
        String suffix = null;
        if (fileName != null) {
            suffix = fileName.substring(fileName.lastIndexOf("."));
        }
        if (StringUtils.isEmpty(suffix)) {
            model.addAttribute("error", "文件类型错误!");
            return "site/setting";
        }

        if (!suffix.equals(".jpeg") && !suffix.equals(".jpg") && !suffix.equals(".png")) {
            model.addAttribute("error", "图片格式错误,请上传jpg,jpeg或png!");
            return "site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;

        // 确定文件存放路径
        File file = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("文件上传失败,服务器异常!");
        }

        // 更新用户头像路径(web路径)
        // http://localhost:8080/community/user/header/xxx.xxx
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeaderUrl(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/user/header/{fileName}",method = RequestMethod.GET)
    public void getHeader (@PathVariable("fileName") String fileName, HttpServletResponse response) {

        // 找服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        // 解析文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        // 响应图片
        response.setContentType("image/" + suffix);
        // 自动关闭流数据
        try (
                FileInputStream fileInputStream = new FileInputStream(fileName);
                OutputStream outputStream = response.getOutputStream()
                ){
            byte[] buffer = new byte[1024];
            int b;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }
    @RequestMapping(path = "/user/modifyPassword", method = RequestMethod.POST)
    public String modifyPassword (@CookieValue("ticket") String ticket, String oldPassword, String newPassword, String confirmPassword, Model model) {
        Map<String, Object> map = userService.modifyPassword(oldPassword, newPassword, confirmPassword, ticket);
        if (map.isEmpty()) {
            loginController.logout(ticket);

            model.addAttribute("msg","您的密码已成功修改,需要重新登录,即将为您跳转到首页");
            model.addAttribute("target","/index");

            return "site/operate-result";
        } else {
            model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            model.addAttribute("confirmPasswordMsg",map.get("confirmPasswordMsg"));

            return "site/setting";
        }
    }
    // 个人主页
    @LoginRequired
    @RequestMapping(path = "/user/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {

        User user = userService.findUserById(userId);

        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        model.addAttribute("user",user);

        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        model.addAttribute("username",user.getUsername());
        model.addAttribute("createTime",user.getCreateTime());
        model.addAttribute("headerUrl",user.getHeaderUrl());

        // 查询关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        // 查询粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        // 查询是否关注
        boolean hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        model.addAttribute("followeeCount",followeeCount);
        model.addAttribute("followerCount",followerCount);
        model.addAttribute("hasFollowed",hasFollowed);

        return "site/profile";
    }
    @RequestMapping(path = "/user/myPost/{userId}", method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Model model, Page page) {
        int rows = discussPostService.findDiscussPostRows(userId);
        page.setRows(rows);
        page.setPath("/user/myPost/" + userId);


        List<DiscussPost> list = discussPostService.findDiscussPosts(userId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list != null){
            for(DiscussPost post:list){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("rows",rows);
        model.addAttribute("nowUserId",userId);
        return "site/my-post";
    }
    @RequestMapping(path = "/user/getCode", method = RequestMethod.POST)
    @ResponseBody
    public String getCode(String email, HttpSession httpSession, HttpServletResponse httpServletResponse) {

        if (httpSession.getAttribute("cd") != null && LocalDateTime.now().isBefore((LocalDateTime) httpSession.getAttribute("cd"))) {
            return CommunityUtil.getJSONString(1, "您点击的太快了,休息一分钟吧!");
        }
        Map<String, Object> map = userService.getCode(email);
        String code = null;

        if (map.containsKey("emailMsg")) {
            return CommunityUtil.getJSONString(1, map.get("emailMsg").toString());
        }

        if (map.containsKey("code")) {
            code = (String) map.get("code");
        }

        if (code != null) {
            httpSession.setAttribute("code", code);
            httpSession.setAttribute("expirationTime",map.get("expirationTime"));
            httpSession.setAttribute("cd",map.get("cd"));

            Cookie cookie = new Cookie("email", email);
            cookie.setMaxAge(300);
            cookie.setPath(contextPath);
            httpServletResponse.addCookie(cookie);
        }
        return CommunityUtil.getJSONString(0, "验证码发送成功,请注意查收!");
    }

    @RequestMapping(path = "/user/forget",method = RequestMethod.GET)
    public String getForgetPage() {
        return "site/forget";
    }

    @RequestMapping(path = "/user/forget",method = RequestMethod.POST)
    public String resetPassword(String password, String email, String code, HttpSession httpSession, Model model, HttpServletRequest httpServletRequest) {
        if (email == null) {
            model.addAttribute("emailMsg", "请输入邮箱!");
            return "site/forget";
        }
        Cookie[] cookies = httpServletRequest.getCookies();
        Cookie cookie = new Cookie("null", "null");
        for (Cookie cookie1 : cookies) {
            if (cookie1.getName().equals("email")) {
                cookie = cookie1;
                break;
            }
        }

        Map<String, Object> map = userService.resetPassword(password, code);
        if (map.isEmpty())
        {
            if (LocalDateTime.now().isAfter((LocalDateTime) httpSession.getAttribute("expirationTime"))) {
                model.addAttribute("codeMsg", "输入的验证码已过期，请重新获取验证码!");
                model.addAttribute("email", email);
                return "site/forget";
            }
            try {
                if (httpSession.getAttribute("code").toString().equals(code)) {

                    if (!cookie.getValue().equals(email)) {
                        model.addAttribute("emailMsg", "验证码和邮箱不匹配,请重新获取验证码!");
                        return "site/forget";
                    }

                    userService.updatePasswordByEmail(email, password);

                    httpSession.removeAttribute("code");
                    httpSession.removeAttribute("expirationTime");
                    httpSession.removeAttribute("cd");

                    cookie.setMaxAge(0);

                    model.addAttribute("msg", "密码修改成功,请重新登录!");
                    model.addAttribute("target", "/login");
                    model.addAttribute("email", email);
                    model.addAttribute("username", userService.selectUserByEmail(email));

                    return "site/operate-result";
                } else {
                    model.addAttribute("codeMsg", "验证码错误!");
                    model.addAttribute("email", email);
                    return "site/forget";
                }
            } catch (Exception e) {
                model.addAttribute("codeMsg", "请先获取验证码!");
                return "site/forget";
            }

        }

        model.addAttribute("codeMsg", map.get("codeMsg"));
        model.addAttribute("passwordMsg", map.get("passwordMsg"));
        model.addAttribute("email", email);

        return "site/forget";
    }

}
