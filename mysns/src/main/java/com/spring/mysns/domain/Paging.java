package com.spring.mysns.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 페이지 나누기 계산기
 *
 * [사용 순서]
 *  1) 전체 개수를 먼저 센다 (COUNT)
 *  2) 그 개수로 Paging 객체를 만든다
 *  3) Paging이 알려주는 offset/size 로 실제 목록을 가져온다
 *
 * 개수를 먼저 세면 없는 페이지(?page=999)를 요청해도
 * 목록 조회 전에 마지막 페이지로 바로잡을 수 있다.
 */
@Getter
public class Paging {

    /** 화면 아래에 한 번에 보여줄 페이지 번호 개수 (예: 1 2 3 4 5) */
    private static final int BLOCK_SIZE = 5;

    private final int page;        // 현재 페이지 (1부터)
    private final int size;        // 한 페이지에 보여줄 개수
    private final int totalCount;  // 전체 개수
    private final int totalPages;  // 전체 페이지 수
    private final int startPage;   // 화면에 표시할 첫 페이지 번호
    private final int endPage;     // 화면에 표시할 마지막 페이지 번호
    private final boolean hasPrev; // 이전 페이지 존재 여부
    private final boolean hasNext; // 다음 페이지 존재 여부

    public Paging(int page, int size, int totalCount) {
        this.size = size;
        this.totalCount = totalCount;

        // 1) 전체 페이지 수 : 글이 없어도 1페이지는 존재해야 화면이 안 깨짐
        //    예) 7개를 3개씩 -> 올림(7/3) = 3페이지
        this.totalPages = (totalCount <= 0)
                ? 1
                : (int) Math.ceil((double) totalCount / size);

        // 2) 요청 페이지 보정 : 0/음수 -> 1페이지, 초과 -> 마지막 페이지
        int fixedPage = Math.max(page, 1);
        this.page = Math.min(fixedPage, this.totalPages);

        // 3) 페이지 번호 묶음 : 5개씩 (1~5, 6~10, ...)
        this.startPage = ((this.page - 1) / BLOCK_SIZE) * BLOCK_SIZE + 1;
        this.endPage = Math.min(this.startPage + BLOCK_SIZE - 1, this.totalPages);

        // 4) 이전/다음 버튼 표시 여부
        this.hasPrev = this.page > 1;
        this.hasNext = this.page < this.totalPages;
    }

    /** SQL OFFSET 값 = 앞에서 건너뛸 개수 (1페이지=0, 2페이지=size, ...) */
    public int getOffset() {
        return (page - 1) * size;
    }

    /**
     * "목록 + 페이지 정보"를 함께 담아 화면으로 보내는 상자
     * 사용 예: Paging.Result<Feed> result
     */
    @Getter
    @AllArgsConstructor
    public static class Result<T> {
        private final List<T> content; // 이번 페이지 목록
        private final Paging paging;   // 페이지 정보
    }
}
