-- =========================================
-- mysns 프로젝트 테이블 생성 스크립트 (MySQL)
-- DB: hello_spring
-- =========================================

CREATE TABLE users (
                       email       VARCHAR(255) NOT NULL PRIMARY KEY,             -- 로그인 ID이자 PK
                       password    VARCHAR(255) NOT NULL,                         -- bcrypt 해시
                       created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE feeds (
                       feed_id     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       user_id     VARCHAR(255) NULL,                             -- 작성자, 탈퇴 시 NULL
                       content     TEXT NULL,
                       created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at  DATETIME NULL DEFAULT NULL,                    -- 등록 시엔 NULL, UPDATE문이 실행될 때만 애플리케이션에서 NOW() 저장
                       CONSTRAINT fk_feeds_user FOREIGN KEY (user_id) REFERENCES users(email)
                           ON DELETE SET NULL,                                    -- 회원 탈퇴 → 피드는 유지, 작성자만 NULL
                       INDEX idx_feeds_user_created (user_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE feed_images (
                             image_id    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                             feed_id     BIGINT UNSIGNED NOT NULL,
                             image_url   VARCHAR(512) NOT NULL,
                             sort_order  TINYINT UNSIGNED NOT NULL DEFAULT 0,
                             created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_images_feed FOREIGN KEY (feed_id) REFERENCES feeds(feed_id)
                                 ON DELETE CASCADE                                      -- 피드 삭제 → 사진 함께 삭제
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comments (
                          comment_id  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                          feed_id     BIGINT UNSIGNED NOT NULL,
                          user_id     VARCHAR(255) NULL,                             -- 작성자, 탈퇴 시 NULL
                          content     VARCHAR(1000) NOT NULL,
                          created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_comments_feed FOREIGN KEY (feed_id) REFERENCES feeds(feed_id)
                              ON DELETE CASCADE,                                     -- 피드 삭제 → 댓글 함께 삭제
                          CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(email)
                              ON DELETE SET NULL,                                    -- 회원 탈퇴 → 댓글은 유지
                          INDEX idx_comments_feed (feed_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;