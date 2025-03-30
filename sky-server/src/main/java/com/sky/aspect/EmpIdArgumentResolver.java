package com.sky.aspect;

import com.sky.annotation.CurrentEmpId;
import com.sky.constant.JwtClaimsConstant;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

@Component
public class EmpIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtProperties jwtProperties;
    @Autowired
    public EmpIdArgumentResolver(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentEmpId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        // 从请求头获取Token
        String token = webRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Token缺失");
        }
        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
        return Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
    }
}