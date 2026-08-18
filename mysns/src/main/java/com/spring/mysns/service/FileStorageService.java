package com.spring.mysns.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 업로드된 사진 파일을 서버 폴더에 저장/삭제하는 서비스
 *
 * [저장 방식]
 *   실제 파일 : (프로젝트)/upload/3f9a2c81....jpg   <- 서버 디스크
 *   DB 저장값 : /upload/3f9a2c81....jpg             <- 웹 경로만
 *
 * DB에 디스크 경로를 넣지 않는 이유:
 * 나중에 폴더 위치가 바뀌어도 DB를 고치지 않아도 되기 때문
 */
@Service
public class FileStorageService {

    /** 허용하는 사진 확장자 (이 외에는 저장하지 않음) */
    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    /** 웹에서 접근할 때 사용하는 경로 앞부분 */
    private static final String WEB_PATH_PREFIX = "/upload/";

    private final Path uploadDir;

    public FileStorageService(@Value("${mysns.upload.dir}") String uploadDir) {
        // 상대경로는 실행 위치에 따라 엉뚱한 곳에 저장될 수 있으므로 절대경로로 보관
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * 사진을 저장하고 DB에 넣을 웹 경로를 반환
     * @return 웹 경로 (예: /upload/abc123.jpg), 파일이 없으면 null
     * @throws IllegalArgumentException 이미지 확장자가 아닐 때
     */
    public String store(MultipartFile file) {
        // 사진을 안 올렸으면 그냥 넘어감 (사진 없는 글도 허용)
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "이미지 파일만 올릴 수 있습니다. (jpg, jpeg, png, gif, webp)");
        }

        // 파일명을 UUID로 변경
        //  - 같은 이름 파일을 올려도 덮어쓰지 않음
        //  - 원본 파일명이 그대로 노출되지 않음
        String savedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        try {
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(savedName);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("사진을 저장하지 못했습니다.", e);
        }

        return WEB_PATH_PREFIX + savedName; // DB에는 웹 경로만
    }

    /**
     * 서버에 저장된 사진 파일 삭제 (피드 삭제 시 파일이 쌓이지 않도록 정리)
     * @param webPath DB에 저장돼 있던 웹 경로 (예: /upload/abc123.jpg)
     */
    public boolean delete(String webPath) {
        if (webPath == null || !webPath.startsWith(WEB_PATH_PREFIX)) {
            return false;
        }

        String fileName = webPath.substring(WEB_PATH_PREFIX.length());

        // [보안] 파일명에 경로가 섞여 있으면 거부
        // "../../..." 같은 값으로 업로드 폴더 밖의 파일을 지우는 것을 막음
        if (fileName.isBlank()
                || fileName.contains("/")
                || fileName.contains("\\")
                || fileName.contains("..")) {
            return false;
        }

        try {
            Path target = uploadDir.resolve(fileName).normalize();

            // 한 번 더 확인 : 계산된 경로가 정말 업로드 폴더 안인가?
            if (!target.startsWith(uploadDir)) {
                return false;
            }
            return Files.deleteIfExists(target);

        } catch (IOException e) {
            // 파일이 사용 중이라 못 지우는 경우 등 -> 글 삭제 자체는 성공해야 하므로 예외 X
            return false;
        }
    }

    /** 파일명에서 확장자만 소문자로 추출 (없으면 빈 문자열) */
    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
