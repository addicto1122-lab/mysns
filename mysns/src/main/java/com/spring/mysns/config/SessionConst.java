package com.spring.mysns.config;

/**
 * 세션 속성 이름을 문자열 하드코딩 대신 상수로 관리
 * - 오타로 인한 버그 방지, 이름 변경 시 한 곳만 수정
 */
public abstract class SessionConst {
    public static final String LOGIN_EMAIL = "loginEmail";
}
