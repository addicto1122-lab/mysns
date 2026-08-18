package com.spring.mysns.service;

import com.spring.mysns.domain.User;
import com.spring.mysns.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 회원 비즈니스 로직
 * - 컨트롤러는 화면 흐름만, 판단(중복 여부 등)은 서비스가 담당
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 회원가입
     * @return true: 가입 성공 / false: 이메일 중복으로 실패
     */
    public boolean signup(User user) {
        // 이메일 중복 검사
        if (userMapper.countByEmail(user.getEmail()) > 0) {
            return false;
        }
        userMapper.insert(user);
        return true;
    }

    /**
     * 로그인
     * @return 일치하는 회원이 있으면 User, 없으면 null
     */
    public User login(String email, String password) {
        return userMapper.findByEmailAndPassword(email, password);
    }
}
