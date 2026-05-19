package com.example.rualingo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ActivityLoggingInterceptor activityLoggingInterceptor;

    public WebConfig(ActivityLoggingInterceptor activityLoggingInterceptor) {
        this.activityLoggingInterceptor = activityLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(activityLoggingInterceptor)
                .addPathPatterns("/api/**");
    }
}
