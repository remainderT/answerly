package org.buaa.project.common.biz.user;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.buaa.project.common.convention.exception.ClientException;
import org.buaa.project.common.convention.result.Results;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.buaa.project.common.enums.UserErrorCodeEnum.USER_TOKEN_NULL;

/**
 * 登录检验过滤器
 */
@RequiredArgsConstructor
public class LoginCheckFilter implements Filter {

    private static final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/answerly/v1/user/login", //登录
            "/api/answerly/v1/user/send-code", //注册时发送验证码
            "/api/answerly/v1/question/page", //分页查看问题
            "/api/answerly/v1/comment/page", //分页查看某题答案
            "/api/answerly/v1/user/captcha", //登录时获取验证码
            "/actuator", //监控
            "/actuator/prometheus",
            "/api/answerly/v1/question/hot", //热门问题
            "/api/answerly/v1/question/suggest" //补全
    );

    private boolean requireLogin(String URI, String method) {
        if (IGNORE_URI.contains(URI)) {
            return false;
        }
        // 注册用户
        if (URI.equals("/api/answerly/v1/user") && method.equals("POST")) {
            return false;
        }
        // 查看所有主题
        if (URI.equals("/api/answerly/v1/category") && method.equals("GET")) {
            return false;
        }
        // 查询题目详情
        if (URI.matches("/api/answerly/v1/question/\\d+")) {
            return false;
        }
        return true;
    }

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod();
        if (requireLogin(requestURI, method) && UserContext.getUsername() == null) {
            returnJson((HttpServletResponse) servletResponse, JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_NULL))));
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(json);

        } catch (IOException e) {
        } finally {
            if (writer != null)
                writer.close();
        }
    }


}
