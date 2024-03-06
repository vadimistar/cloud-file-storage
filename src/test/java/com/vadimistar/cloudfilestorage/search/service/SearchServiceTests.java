package com.vadimistar.cloudfilestorage.search.service;

import com.vadimistar.cloudfilestorage.common.repository.MinioRepository;
import com.vadimistar.cloudfilestorage.common.MinioTestUnits;
import com.vadimistar.cloudfilestorage.file.service.FileService;
import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

@SpringBootTest
@Testcontainers
public class SearchServiceTests {

    @Autowired
    private SearchService searchService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MinioRepository minioRepository;

    @BeforeEach
    public void beforeEach() {
        if (minioRepository.isBucketExists()) {
            minioRepository.removeObjects("");
            minioRepository.removeBucket();
        }
        minioRepository.makeBucket();
    }

    @SneakyThrows
    @Test
    public void searchFiles_returnsFilesWithQueryInName() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "abcd");
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "eabcd");
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "abcde");
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "bcde");
        List<FoundFileDto> foundFiles = searchService.searchFiles(USER_ID, "abc")
                .sorted(Comparator.comparing(FoundFileDto::getName))
                .toList();
        Assertions.assertEquals(3, foundFiles.size());
        Assertions.assertEquals("abcd", foundFiles.get(0).getName());
        Assertions.assertEquals("abcde", foundFiles.get(1).getName());
        Assertions.assertEquals("eabcd", foundFiles.get(2).getName());
    }

    private static final long USER_ID = 1;

    private ByteArrayResource getMockFile() {
        return new ByteArrayResource("Mock file contents".getBytes(StandardCharsets.UTF_8));
    }

    @Container
    private static final MinIOContainer minioContainer = MinioTestUnits.createMinIOContainer();

    @DynamicPropertySource
    public static void minioProperties(DynamicPropertyRegistry registry) {
        MinioTestUnits.addMinioProperties(registry, minioContainer);
    }

}
