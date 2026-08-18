package com.spring.mysns.repository;

import com.spring.mysns.domain.FeedImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 피드 사진(feed_images) Mapper 인터페이스
 */
@Mapper
public interface FeedImageMapper {

    // 사진 등록
    int insert(FeedImage image);

    // 특정 피드의 사진 목록 (순서대로)
    List<FeedImage> findByFeedId(@Param("feedId") Long feedId);

    // 특정 피드의 사진 전부 삭제
    int deleteByFeedId(@Param("feedId") Long feedId);
}
