package com.sky.filter;

import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;
import jakarta.servlet.*;

/**
 * 清理 ThreadLocal 中的 empId
 */
@Slf4j
@Component
public class ClearContextFilter implements Filter {

    // 过滤器逻辑
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } finally {
            if (BaseContext.isIdSet()) { // 仅在 empId 被设置过时清理
                BaseContext.removeCurrentId();
            }
        }
    }
}