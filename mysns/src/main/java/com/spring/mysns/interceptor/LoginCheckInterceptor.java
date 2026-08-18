package com.spring.mysns.interceptor;

import com.spring.mysns.config.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인 체크 인터셉터
 * - 컨트롤러 실행 전에 세션에 로그인 정보가 있는지 확인
 * - 없으면 로그인 화면으로 돌려보냄 (실제 SNS처럼 비로그인 접근 차단)
 */
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // 세션이 없거나 로그인 정보가 없으면 로그인 페이지로 리다이렉트
        if (session == null || session.getAttribute(SessionConst.LOGIN_EMAIL) == null) {
            response.sendRedirect("/login?required=true");
            return false; // 컨트롤러 진행 중단
        }
        return true; // 로그인 상태 -> 요청 계속 진행
    }
}
