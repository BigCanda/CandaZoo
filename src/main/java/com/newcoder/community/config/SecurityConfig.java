package com.newcoder.community.config;

import com.newcoder.community.services.DiscussPostService;
import com.newcoder.community.services.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Action;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
    @Override
    public void configure(WebSecurity webSecurity) throws Exception {
        webSecurity.ignoring().antMatchers("/resource/**");
    }
    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        // 授权
        httpSecurity.authorizeHttpRequests()
                .antMatchers(
                        "/user/setting",
                            "/user/upload",
                            "/discuss/add",
                            "/comment/add/**",
                            "/letter/**",
                            "/notice/**",
                            "/follow",
                            "/unfollow",
                            "/like",
                            "/user/modifyPassword/**",
                            "/user/profile/**"

                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                ).antMatchers(
                        "/discuss/delete",
                        "/discuss/top",
                        "/discuss/wonderful"
                ).hasAnyAuthority(
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                ).antMatchers(
                        "/discuss/delete"
                ).hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll();


        // 权限不足
        // 没有登录
        httpSecurity.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你还没有登录!"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你没有操作的权限!"));
                        } else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });
        // spring security自动拦截名为logout的请求并且自动退出
        // 要覆盖这个的方法
        httpSecurity.logout().logoutUrl("/securityLogout");
    }
}
