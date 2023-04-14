package com.newcoder.community.controller;//Controller是用于回应前端的请求

import com.newcoder.community.services.AlphaServices;
import com.newcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/Alpha")//controller嵌套
public class AlphaController {
    @Autowired
    private AlphaServices alphaServices;
    @GetMapping("/hello")//Get方法获取映射
    @ResponseBody
    public String Hello(){
        return "hello world!";
    }
    @RequestMapping("/Atao")

    public String Atao(){
        return "Atao is the best programmer!";
    }
    @RequestMapping("/getData")
    public String getData(){
        return alphaServices.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        //获取请求数据
        System.out.println(httpServletRequest.getMethod());
        System.out.println(httpServletRequest.getHeaderNames());
        Enumeration<String> enumeration = httpServletRequest.getHeaderNames();
        while(enumeration.hasMoreElements()){
            String name = enumeration.nextElement();//获取返回的数据
            String value = httpServletRequest.getHeader(name);//传入name得到相对应的值
            System.out.println(name+": "+value);
        }
        System.out.println(httpServletRequest.getParameter("code"));
        //返回相应数据
        httpServletResponse.setContentType("text/html;charset=utf-8");
        try(PrintWriter writer = httpServletResponse.getWriter()){

            writer.write("<h1>Atao,YES!<h1>");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    //页面访问GET请求，将参数用?传递
    // /students?current = 1 & limit = 20
    @RequestMapping(path = "/students",method = RequestMethod.GET)
    public String getStudent(){
        return "site/student";
    }
    //路径访问GET请求，将参数写入路径
    // /student/xxx
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student";
    }
    // POST请求，当参数数量较多时使用POST请求

    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return "success!";
    }

    //响应HTML数据

    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher () {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","张三");
        mav.addObject("age",30);
        mav.setViewName("/demo/view");
        return mav;
    }
    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool (Model model) {
        model.addAttribute("name","FAFU");
        model.addAttribute("age",87);
        return "/demo/view";
    }
     /*public ModelAndView getSchool() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","Fafu");
        mav.addObject("age",87);
        mav.setViewName("/demo/view");
        return mav;
    }*/

    //响应JSON数据(异步数据，即只返回数据)
    //Java对象 -> JSON字符串 -> JS对象

    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody//访问的是JSON就要写
    public Map<String , Object> getEmp () {
        Map<String, Object> emp = new HashMap<>();
        emp.put("name","张三");
        emp.put("age",25);
        emp.put("salary",10000);
        return emp;
    }

    @RequestMapping(path = "/emps",method = RequestMethod.GET)
    @ResponseBody//访问的是JSON就要写
    public List<Map<String , Object>> getEmps(){
        List<Map<String , Object>> list = new ArrayList<>();
        Map<String, Object> emp = new HashMap<>();
        emp.put("name","张三");
        emp.put("age",25);
        emp.put("salary",10000);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name","李四");
        emp.put("age",26);
        emp.put("salary",12000);
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name","王五");
        emp.put("age",27);
        emp.put("salary",14000);
        list.add(emp);

        return list;
    }

    // 23.3.8
    @RequestMapping(path = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie (HttpServletResponse httpServletResponse) {

        // 创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());

        // 设置生效范围
        cookie.setPath("/community/Alpha");

        // 设置cookie的生存时间
        cookie.setMaxAge(60 * 10);

        // 发送cookie
        httpServletResponse.addCookie(cookie);
        return "set cookie";
    }
    // 23.3.8
    // cookie测试
    // cookie缺点：不安全，增加流量消耗
    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie (@CookieValue("code") String code) {
        System.out.println(code);
        return "get cookie";
    }
    // session消耗服务器资源比较多，
    // 服务器会访问nginx（负载均衡服务器），并且在分布式下无法同步用户数据（一个服务器有，另一个没有）
    // 解决办法：黏性session（把ip分发给古风服务器，会导致负载不均衡）、同步session（把session同步给每一台服务器，但是影响性能）、共享session（使用一台服务器专门存储session，但是稳定性比较差）
    // 最好的方法是存储到Redis数据库中
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession (HttpSession httpSession) {
        httpSession.setAttribute("id",1);
        httpSession.setAttribute("name","123");
        return "set session";
    }

    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession (HttpSession httpSession) {
        System.out.println(httpSession.getAttribute("id"));
        System.out.println(httpSession.getAttribute("name"));
        return "get session";
    }

    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    public String restAjax (String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功");
    }
}
