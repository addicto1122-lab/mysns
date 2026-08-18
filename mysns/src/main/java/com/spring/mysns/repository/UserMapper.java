package com.spring.mysns.repository;

import com.spring.mysns.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 회원 Mapper 인터페이스
 * - 실제 SQL은 resources/mapper/UserMapper.xml 에 작성
 * - 파라미터가 2개 이상이면 @Param 으로 이름을 명시해야 XML에서 안전하게 사용 가능
 */
@Mapper
public interface UserMapper {

    // 회원 등록 -> 성공 시 1 반환
    int insert(User user);

    // 이메일 + 비밀번호로 회원 조회 (로그인용)
    User findByEmailAndPassword(@Param("email") String email,
                                @Param("password") String password);

    // 이메일 중복 확인 -> 존재하면 1 이상
    int countByEmail(@Param("email") String email);
}
