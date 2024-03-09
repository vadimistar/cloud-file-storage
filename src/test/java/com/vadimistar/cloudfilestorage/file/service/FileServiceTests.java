package com.vadimistar.cloudfilestorage.file.service;

import com.vadimistar.cloudfilestorage.minio.repository.MinioRepository;
import com.vadimistar.cloudfilestorage.common.TestUtils;
import com.vadimistar.cloudfilestorage.file.exception.FileNotFoundException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@SpringBootTest
@ActiveProfiles("dev")
@Testcontainers
public class FileServiceTests {

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
    public void uploadFile_withoutPrefixSlash_savesFileInUserDirectory() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "a/b/c");
        InputStream inputStream = minioRepository.getObject(USER_ID_DIRECTORY + "a/b/c");
        Assertions.assertArrayEquals(mockFile.getByteArray(), inputStream.readAllBytes());
    }

    @SneakyThrows
    @Test
    public void uploadFile_emptyFileWithPrefixSlash_savesFileInUserDirectory() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "/a/b/c");
        InputStream inputStream = minioRepository.getObject(USER_ID_DIRECTORY + "a/b/c");
        Assertions.assertArrayEquals(mockFile.getByteArray(), inputStream.readAllBytes());
    }

    @SneakyThrows
    @Test
    public void renameFile_fileExists_toAnotherName_createsAnotherFile_removesOldFile() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "/a/b/c");
        fileService.renameFile(USER_ID, "/a/b/c", "d");
        Assertions.assertTrue(minioRepository.isObjectExists(USER_ID_DIRECTORY + "a/b/d"));
        Assertions.assertFalse(minioRepository.isObjectExists(USER_ID_DIRECTORY + "/a/b/c"));
    }

    @SneakyThrows
    @Test
    public void renameFile_fileExists_toAnotherName_returnsValidNewPath() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "/a/b/c");
        String newPath = fileService.renameFile(USER_ID, "/a/b/c", "d");
        Assertions.assertEquals("/a/b/d", newPath);
    }

    @Test
    public void renameFile_fileNotExists_throwsFileNotFoundException() {
        Assertions.assertThrows(
                FileNotFoundException.class,
                () -> fileService.renameFile(USER_ID, "abc", "d")
        );
    }

    @SneakyThrows
    @Test
    public void renameFile_fileExists_toSameName_returnsOldPath() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "/a/b/c");
        Assertions.assertEquals(
                "/a/b/c",
                fileService.renameFile(USER_ID, "/a/b/c", "c")
        );
    }

    @SneakyThrows
    @Test
    public void downloadFile_fileExists_returnsContentsOfFile() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "/a/b/c");
        byte[] fileContents = fileService.downloadFile(USER_ID, "/a/b/c");
        Assertions.assertArrayEquals(mockFile.getByteArray(), fileContents);
    }

    @Test
    public void downloadFile_fileNotExists_throwsFileNotFoundException() {
        Assertions.assertThrows(
                FileNotFoundException.class,
                () -> fileService.downloadFile(USER_ID, "/a/b/c")
        );
    }

    @SneakyThrows
    @Test
    public void isFileExists_fileExists_returnsTrue() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "/a/b/c");
        Assertions.assertTrue(fileService.isFileExists(USER_ID, "/a/b/c"));
    }

    @Test
    public void isFileExists_fileNotExists_returnsFalse() {
        Assertions.assertFalse(fileService.isFileExists(USER_ID, "/a/b/c"));
    }

    private static final long USER_ID = 1;
    private static final String USER_ID_DIRECTORY = "user-%d-files/".formatted(USER_ID);

    private static ByteArrayResource getMockFile() {
        return new ByteArrayResource(MOCK_FILE_CONTENTS);
    }

    private static final byte[] MOCK_FILE_CONTENTS = "Mock file content".getBytes(StandardCharsets.UTF_8);

    @Container
    private static final MinIOContainer minioContainer = TestUtils.createMinIOContainer();

    @DynamicPropertySource
    public static void minioProperties(DynamicPropertyRegistry registry) {
        TestUtils.addMinioProperties(registry, minioContainer);
    }

}