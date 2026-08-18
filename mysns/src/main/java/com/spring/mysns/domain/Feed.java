package com.spring.mysns.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 피드 도메인
 * - map-underscore-to-camel-case 옵션이 켜져 있으므로
 *   DB 컬럼(snake_case) -> 자바 필드(camelCase) 자동 매핑됨
 * - user_id 컬럼은 users.email 을 참조 (작성자 이메일)
 */
@Data
public class Feed {
    private Long feedId;              // feed_id
    private String userId;            // user_id (users.email 참조)
    private String content;           // 피드 내용
    private LocalDateTime createdAt;  // created_at
    private LocalDateTime updatedAt;  // updated_at

    /**
     * 대표 사진 웹 경로 (feed_images 서브쿼리로 채워짐)
     * 사진이 없는 피드는 null
     */
    private String imageUrl;          // image_url
}
