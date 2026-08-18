package com.spring.mysns.config;

import com.spring.mysns.interceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 웹 관련 설정
 * - 로그인 체크 인터셉터 등록 및 적용/제외 경로 지정
 * - 업로드된 사진 폴더를 웹 경로(/upload/**)로 노출
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 업로드된 사진이 실제로 저장되는 폴더 (application.properties에서 지정) */
    @Value("${mysns.upload.dir}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(1)
                .addPathPatterns("/**") // 전체 경로에 적용하고
                .excludePathPatterns(   // 로그인 없이 접근 가능한 경로만 제외
                        "/", "/login", "/signup", "/logout", "/weather","/map",
                        "/css/**", "/js/**", "/images/**", "/upload/**", "/error"
                );
    }

    /**
     * 브라우저가 /upload/abc123.jpg 를 요청하면
     * 서버는 (업로드폴더)/abc123.jpg 파일을 찾아서 응답
     *
     * DB에는 웹 경로(/upload/abc123.jpg)만 저장하므로
     * 실제 폴더 위치가 바뀌어도 이 설정만 고치면 됨
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(absolutePath);
    }
}
