package com.spring.mysns.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 피드에 붙은 사진 (DB: feed_images 테이블)
 *
 * - imageUrl 에는 디스크 경로(C:/...)가 아니라 웹 경로(/upload/xxx.jpg)만 저장
 *   -> 나중에 저장 폴더를 옮겨도 DB를 고칠 필요가 없음
 * - 지금은 피드당 한 장만 올리지만 테이블은 여러 장 담을 수 있게 설계 (sort_order)
 */
@Data
@NoArgsConstructor
public class FeedImage {

    private Long imageId;             // image_id
    private Long feedId;              // feed_id (feeds 참조)
    private String imageUrl;          // image_url (웹 경로)
    private Integer sortOrder;        // sort_order (0이 가장 앞)
    private LocalDateTime createdAt;  // created_at

    /** 새 사진 등록용 생성자 */
    public FeedImage(Long feedId, String imageUrl, Integer sortOrder) {
        this.feedId = feedId;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }
}
