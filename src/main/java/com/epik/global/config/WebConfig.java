package com.epik.global.config;

import com.epik.global.auth.resolver.AuthUserArgumentResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
      log.info(">>> WebConfig.addArgumentResolvers 호출");
        resolvers.add(new AuthUserArgumentResolver());
    }
}
