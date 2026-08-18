package com.spring.mysns.controller;

import com.spring.mysns.config.SessionConst;
import com.spring.mysns.domain.User;
import com.spring.mysns.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 로그인 / 로그아웃 / 회원가입 담당
 *
 * [핵심 패턴] PRG (Post-Redirect-Get)
 * - POST 처리 후에는 항상 redirect -> 새로고침 시 중복 제출 방지
 * - redirect 후 화면에 전달할 1회성 메시지는 RedirectAttributes.addFlashAttribute 사용
 *   (URL에 노출되지 않고, 한 번 읽으면 사라짐)
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 기본 도메인 요청 시 로그인 페이지로 이동
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    // 로그인 화면
    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String required,
                            RedirectAttributes redirectAttributes,
                            HttpServletRequest request) {
        // 이미 로그인된 상태라면 피드 화면으로 보냄 (실제 SNS 동작)
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SessionConst.LOGIN_EMAIL) != null) {
            return "redirect:/feeds";
        }
        // 인터셉터가 돌려보낸 경우 안내 메시지 표시
        if (required != null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요한 서비스입니다.");
            return "redirect:/login";
        }
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {

        User user = userService.login(email, password);

        if (user == null) {
            // 로그인 실패 -> 로그인 화면으로 이동 + 실패 메시지
            redirectAttributes.addFlashAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
            // 이메일은 다시 입력하지 않도록 유지 (비밀번호는 보안상 유지하지 않음)
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/login";
        }

        // 로그인 성공 -> 세션 생성 후 피드 화면으로 이동
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_EMAIL, user.getEmail());
        return "redirect:/feeds";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 만료
        }
        redirectAttributes.addFlashAttribute("message", "로그아웃되었습니다.");
        return "redirect:/login"; // 뷰 직접 반환 대신 redirect (PRG)
    }

    // 회원가입 화면
    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@ModelAttribute User user,
                         RedirectAttributes redirectAttributes) {

        boolean success = userService.signup(user);

        if (!success) {
            // 이메일 중복 -> 회원가입 화면으로 이동 + 실패 메시지
            redirectAttributes.addFlashAttribute("error", "이미 사용 중인 이메일입니다.");
            return "redirect:/signup";
        }

        // 가입 성공 -> 로그인 화면으로 이동 + 성공 메시지
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
        return "redirect:/login";
    }
}
