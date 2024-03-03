package com.vadimistar.cloudfilestorage.file.service;

import com.vadimistar.cloudfilestorage.adapters.minio.Minio;
import com.vadimistar.cloudfilestorage.common.exceptions.FileAlreadyExistsException;
import com.vadimistar.cloudfilestorage.file.exception.FileNotFoundException;
import io.minio.GetObjectResponse;
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

@SpringBootTest
@Testcontainers
public class FileServiceTests {

    @Autowired
    private FileService fileService;

    @Autowired
    private Minio minio;

    @BeforeEach
    public void beforeEach() {
        if (minio.isBucketExists()) {
            minio.removeObjects("");
            minio.removeBucket();
        }
        minio.makeBucket();
    }

    @SneakyThrows
    @Test
    public void uploadFile_withoutPrefixSlash_savesFileInUserDirectory() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "a/b/c");
        GetObjectResponse getObjectResponse = minio.getObject(USER_ID_DIRECTORY + "a/b/c");
        Assertions.assertArrayEquals(mockFile.getByteArray(), getObjectResponse.readAllBytes());
    }

    @SneakyThrows
    @Test
    public void uploadFile_emptyFileWithPrefixSlash_savesFileInUserDirectory() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "/a/b/c");
        GetObjectResponse getObjectResponse = minio.getObject(USER_ID_DIRECTORY + "a/b/c");
        Assertions.assertArrayEquals(mockFile.getByteArray(), getObjectResponse.readAllBytes());
    }

    @SneakyThrows
    @Test
    public void renameFile_fileExists_toAnotherName_createsAnotherFile_removesOldFile() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "/a/b/c");
        fileService.renameFile(USER_ID, "/a/b/c", "d");
        Assertions.assertTrue(minio.statObject(USER_ID_DIRECTORY + "a/b/d").isPresent());
        Assertions.assertFalse(minio.statObject(USER_ID_DIRECTORY + "/a/b/c").isPresent());
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
    public void renameFile_fileExists_toSameName_throwsFileAlreadyExistsException() {
        ByteArrayResource mockFile = getMockFile();
        fileService.uploadFile(USER_ID, mockFile.getInputStream(), mockFile.contentLength(), "/a/b/c");
        Assertions.assertThrows(
                FileAlreadyExistsException.class,
                () -> fileService.renameFile(USER_ID, "/a/b/c", "c")
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
    private static final MinIOContainer minioContainer = new MinIOContainer("minio/minio:latest");

    @DynamicPropertySource
    public static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint", minioContainer::getS3URL);
        registry.add("minio.access-key", minioContainer::getUserName);
        registry.add("minio.secret-key", minioContainer::getPassword);
        registry.add("minio.bucket-name", () -> MINIO_BUCKET_NAME);
    }

    private static final String MINIO_BUCKET_NAME = "user-files-test";

}