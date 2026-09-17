package com.hospital.config;

import com.hospital.interceptor.TokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：跨域 + 登录拦截器注册与白名单。
 * 说明：公开可浏览的资源（医院/医生/疾病/文章/科室/搜索）与登录注册、支付回调、接口文档放行；
 * 其余 /api/** 需要登录。
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenInterceptor tokenInterceptor;

    /** 无需登录即可访问的路径 */
    private static final String[] WHITE_LIST = {
            "/api/auth/**",
            "/api/hospitals/**",
            "/api/doctors/**",
            "/api/departments/**",
            "/api/diseases/**",
            "/api/articles/**",
            // 公开搜索端点（历史/重建索引仍需登录，故不整体放行 /api/search/**）
            "/api/search/hospitals",
            "/api/search/doctors",
            "/api/search/diseases",
            "/api/search/articles",
            "/api/search/hot",
            "/api/reviews/doctor/**",
            "/api/pay/alipay/notify",
            "/api/common/**",
            // 接口文档
            "/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**",
            "/favicon.ico", "/error"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(WHITE_LIST);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("token")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
