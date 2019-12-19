/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.oauth2;

import com.google.gson.Gson;
import io.renren.common.utils.HttpContextUtils;
import io.renren.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * oauth2过滤器
 *
 * @author Mark sunlightcs@gmail.com
 */
public class OAuth2Filter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        // 获取请求中的 token
        String token = getRequestToken((HttpServletRequest) request);
        // 如果不存在，则返回 null
        if (StringUtils.isBlank(token)) {
            return null;
        }

        // 创建 OAuth2Token 对象
        return new OAuth2Token(token);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return ((HttpServletRequest) request).getMethod().equals(RequestMethod.OPTIONS.name());
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        // 获取请求中的 token 。如果 token 不存在，直接返回 401 ，认证不通过
        String token = getRequestToken((HttpServletRequest) request);
        if (StringUtils.isBlank(token)) {
            // 设置响应 Header
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());

            // 返回认证不通过
            String json = new Gson().toJson(R.error(HttpStatus.SC_UNAUTHORIZED, "invalid token"));
            httpResponse.getWriter().print(json);

            // 返回 false
            return false;
        }

        // 执行登陆逻辑，实际执行的是基于 Token 进行认证。
        return executeLogin(request, response);
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        // 设置响应 Header
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setContentType("application/json;charset=utf-8");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Origin", HttpContextUtils.getOrigin());
        try {
            // 处理登录失败的异常
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            R r = R.error(HttpStatus.SC_UNAUTHORIZED, throwable.getMessage());

            // 返回认证不通过
            String json = new Gson().toJson(r);
            httpResponse.getWriter().print(json);
        } catch (IOException ignored) {
        }

        // 返回 false
        return false;
    }

    /**
     * 获取请求的token
     */
    private String getRequestToken(HttpServletRequest httpRequest) {
        // 优先，从 header 中获取 token
        String token = httpRequest.getHeader("token");

        // 次之，如果 header 中不存在 token ，则从参数中获取 token
        if (StringUtils.isBlank(token)) {
            token = httpRequest.getParameter("token");
        }

        return token;
    }

}
