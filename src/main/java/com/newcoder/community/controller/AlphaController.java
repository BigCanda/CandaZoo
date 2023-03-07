package com.newcoder.community.controller;//Controller是用于回应前端的请求

import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.User;
import com.newcoder.community.services.AlphaServices;
import com.newcoder.community.services.DiscussPostService;
import com.newcoder.community.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/Alpha")//controller嵌套
public class AlphaController {
    @Autowired
    private AlphaServices alphaServices;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
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
    @ResponseBody
    public String getStudent(
            @RequestParam(name = "current",required = false,defaultValue = "1") int current,//传参
            @RequestParam(name = "limit",required = false,defaultValue = "1") int limit){
            System.out.println(current);
            System.out.println(limit);
        return "some students!";
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
    public ModelAndView getTeacher(){
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","张三");
        mav.addObject("age",30);
        mav.setViewName("/demo/view");
        return mav;
    }
    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model){
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
    public Map<String , Object> getEmp(){
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



}
