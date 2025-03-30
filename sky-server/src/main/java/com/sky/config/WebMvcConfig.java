package com.sky.config;

import com.sky.aspect.EmpIdArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final EmpIdArgumentResolver empIdArgumentResolver;

    @Autowired
    public WebMvcConfig(EmpIdArgumentResolver empIdArgumentResolver) {
        this.empIdArgumentResolver = empIdArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(empIdArgumentResolver);
    }
}