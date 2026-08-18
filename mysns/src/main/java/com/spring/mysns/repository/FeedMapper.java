package com.spring.mysns.repository;

import com.spring.mysns.domain.Feed;
import com.spring.mysns.domain.Paging;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 피드 Mapper 인터페이스
 * - 실제 SQL은 resources/mapper/FeedMapper.xml 에 작성
 * - user_id 컬럼은 users.email 을 참조
 */
@Mapper
public interface FeedMapper {

    // 전체 피드 개수 (페이지 수 계산용)
    int countAll();

    // 한 페이지 분량의 피드 목록 (최신순, 대표 사진 포함)
    List<Feed> findPage(Paging paging);

    // 피드 작성
    int insert(Feed feed);

    // 피드 단건 조회
    Feed findById(@Param("feedId") Long feedId);

    // 피드 수정 (본인 글만 수정되도록 user_id 조건 포함)
    int update(@Param("feedId") Long feedId,
               @Param("userId") String userId,
               @Param("content") String content);

    // 피드 삭제 (본인 글만 삭제되도록 user_id 조건 포함)
    int delete(@Param("feedId") Long feedId,
               @Param("userId") String userId);
}
