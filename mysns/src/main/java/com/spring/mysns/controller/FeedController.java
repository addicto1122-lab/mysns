package com.spring.mysns.controller;

import com.spring.mysns.config.SessionConst;
import com.spring.mysns.domain.Feed;
import com.spring.mysns.domain.Paging;
import com.spring.mysns.service.FeedService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 피드 화면 담당 (작성 / 목록 / 수정 / 삭제)
 * - 로그인 여부는 LoginCheckInterceptor가 먼저 검사하므로
 *   여기 도달했다면 세션에 로그인 정보가 항상 존재
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/feeds")
public class FeedController {

    private final FeedService feedService;

    /**
     * 피드 목록 화면 (주소 예: /feeds 또는 /feeds?page=2)
     */
    @GetMapping
    public String feedList(@RequestParam(defaultValue = "1") int page,
                           HttpSession session, Model model) {

        Paging.Result<Feed> result = feedService.getFeedPage(page);

        model.addAttribute("loginEmail", session.getAttribute(SessionConst.LOGIN_EMAIL));
        model.addAttribute("feeds", result.getContent());
        model.addAttribute("paging", result.getPaging());
        return "feed/list";
    }

    /**
     * 피드 작성 (사진은 선택)
     * - 화면 form에 enctype="multipart/form-data" 가 있어야 파일이 넘어옴
     */
    @PostMapping
    public String write(@RequestParam String content,
                        @RequestParam(required = false) MultipartFile image,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute(SessionConst.LOGIN_EMAIL);

        // 공백만 입력한 경우 방어
        if (content == null || content.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "내용을 입력해주세요.");
            return "redirect:/feeds";
        }

        try {
            feedService.write(userId, content.trim(), image);
            redirectAttributes.addFlashAttribute("message", "피드가 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            // 이미지가 아닌 파일을 올린 경우 (@Transactional 덕분에 글도 저장되지 않음)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        // 새 글은 항상 맨 위에 있으므로 1페이지로 이동
        return "redirect:/feeds";
    }

    /**
     * 피드 수정
     * - page 파라미터: 수정 후 보고 있던 페이지로 되돌아가기 위함
     */
    @PostMapping("/{feedId}/edit")
    public String edit(@PathVariable Long feedId,
                       @RequestParam String content,
                       @RequestParam(defaultValue = "1") int page,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute(SessionConst.LOGIN_EMAIL);

        if (content == null || content.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "내용을 입력해주세요.");
            return "redirect:/feeds?page=" + page;
        }

        // 서버에서도 본인 글인지 검증 (화면에서 버튼을 숨겨도 요청 위조 가능하므로)
        boolean updated = feedService.update(feedId, userId, content.trim());
        if (updated) {
            redirectAttributes.addFlashAttribute("message", "피드가 수정되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "본인이 작성한 피드만 수정할 수 있습니다.");
        }
        return "redirect:/feeds?page=" + page;
    }

    /**
     * 피드 삭제 (사진 파일도 함께 정리)
     */
    @PostMapping("/{feedId}/delete")
    public String delete(@PathVariable Long feedId,
                         @RequestParam(defaultValue = "1") int page,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute(SessionConst.LOGIN_EMAIL);

        boolean deleted = feedService.delete(feedId, userId);
        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "피드가 삭제되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "본인이 작성한 피드만 삭제할 수 있습니다.");
        }
        // 삭제 후에도 보고 있던 페이지 유지 (Paging이 범위를 벗어나면 자동 보정)
        return "redirect:/feeds?page=" + page;
    }
}
