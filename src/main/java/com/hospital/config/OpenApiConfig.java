package com.hospital.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档（Knife4j / OpenAPI3）。访问地址：/doc.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hospitalOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("在线医疗挂号系统 API")
                .description("用户认证 / 医疗资源 / 预约挂号 / 电话咨询 / 支付 / 互动 / 搜索")
                .version("1.0.0"));
    }
}
