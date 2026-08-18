package com.spring.mysns.service;

import com.spring.mysns.domain.Feed;
import com.spring.mysns.domain.FeedImage;
import com.spring.mysns.domain.Paging;
import com.spring.mysns.repository.FeedImageMapper;
import com.spring.mysns.repository.FeedMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 피드 비즈니스 로직
 * - userId 파라미터에는 로그인한 사용자의 email이 들어옴 (feeds.user_id -> users.email 참조)
 */
@Service
@RequiredArgsConstructor
public class FeedService {

    /** 한 페이지에 보여줄 피드 개수 */
    private static final int PAGE_SIZE = 3;

    private final FeedMapper feedMapper;
    private final FeedImageMapper feedImageMapper;
    private final FileStorageService fileStorageService;

    /**
     * 한 페이지 분량의 피드 목록 + 페이지 정보 조회
     *
     * [순서가 중요]
     * 1) COUNT로 전체 개수를 먼저 센다
     * 2) Paging이 페이지 번호를 보정한다 (없는 페이지 요청 -> 마지막 페이지)
     * 3) 보정된 offset/size로 목록을 가져온다
     */
    public Paging.Result<Feed> getFeedPage(int page) {
        int totalCount = feedMapper.countAll();
        Paging paging = new Paging(page, PAGE_SIZE, totalCount);
        List<Feed> feeds = feedMapper.findPage(paging);
        return new Paging.Result<>(feeds, paging);
    }

    /**
     * 피드 작성 (사진은 선택)
     *
     * @Transactional : 글 저장 후 사진 저장에서 실패하면
     *                  글 INSERT도 함께 취소(롤백)되어 반쪽짜리 데이터가 남지 않음
     * @throws IllegalArgumentException 이미지가 아닌 파일을 올린 경우
     */
    @Transactional
    public void write(String userId, String content, MultipartFile image) {
        // 1) 글 저장 (저장 후 feed.feedId에 자동 생성된 번호가 채워짐)
        Feed feed = new Feed();
        feed.setUserId(userId);
        feed.setContent(content);
        feedMapper.insert(feed);

        // 2) 사진이 있으면 저장 (없으면 그냥 넘어감)
        String imageUrl = fileStorageService.store(image);
        if (imageUrl != null) {
            // 지금은 사진 한 장만 올리므로 순서는 항상 0
            feedImageMapper.insert(new FeedImage(feed.getFeedId(), imageUrl, 0));
        }
    }

    /**
     * 피드 수정 - 작성자 본인일 때만 수정됨
     * @return true: 수정 성공 / false: 본인 글이 아니거나 존재하지 않음
     */
    public boolean update(Long feedId, String userId, String content) {
        return feedMapper.update(feedId, userId, content) > 0;
    }

    /**
     * 피드 삭제 - 작성자 본인일 때만 삭제됨
     * DB의 사진 기록과 디스크의 사진 파일도 함께 정리
     *
     * @return true: 삭제 성공 / false: 본인 글이 아니거나 존재하지 않음
     */
    @Transactional
    public boolean delete(Long feedId, String userId) {
        // 1) 작성자 본인인지 먼저 확인
        Feed feed = feedMapper.findById(feedId);
        if (feed == null || !feed.getUserId().equals(userId)) {
            return false;
        }

        // 2) 파일 삭제를 위해 사진 목록을 미리 확보 (DB에서 지우기 전에!)
        List<FeedImage> images = feedImageMapper.findByFeedId(feedId);

        // 3) 자식(사진) -> 부모(피드) 순서로 삭제 (FK 제약 때문)
        feedImageMapper.deleteByFeedId(feedId);
        feedMapper.delete(feedId, userId);

        // 4) 디스크의 실제 파일 정리
        //    DB 삭제가 끝난 뒤에 지우므로, 여기서 실패해도 화면에는 영향 없음
        for (FeedImage image : images) {
            fileStorageService.delete(image.getImageUrl());
        }
        return true;
    }
}
