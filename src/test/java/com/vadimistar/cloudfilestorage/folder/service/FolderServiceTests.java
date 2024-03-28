package com.vadimistar.cloudfilestorage.folder.service;

import com.vadimistar.cloudfilestorage.common.TestUtils;
import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.file.service.FileService;
import com.vadimistar.cloudfilestorage.folder.exception.FolderNotFoundException;
import com.vadimistar.cloudfilestorage.minio.dto.MinioObjectDto;
import com.vadimistar.cloudfilestorage.minio.exception.ResourceAlreadyExistsException;
import com.vadimistar.cloudfilestorage.minio.repository.MinioRepository;
import com.vadimistar.cloudfilestorage.common.utils.MinioUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipInputStream;

@SpringBootTest
@ActiveProfiles("dev")
@Testcontainers
public class FolderServiceTests {

    @Autowired
    private FolderService folderService;

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
        folderService.createFolder(USER_ID, "/");
    }

    @SneakyThrows
    @Test
    public void createFolder_resourceAtThisPathNotExists_createsEmptyObject() {
        folderService.createFolder(USER_ID, "a/b/c");
        Assertions.assertEquals(0, minioRepository.getObject(USER_ID_DIRECTORY + "a/b/c/").readAllBytes().length);
    }

    @Test
    public void createFolder_resourceAlreadyExistsAtPath_throwsResourceAlreadyExistsException() {
        folderService.createFolder(USER_ID, "a/b/c");
        Assertions.assertThrows(
                ResourceAlreadyExistsException.class,
                () -> folderService.createFolder(USER_ID, "a/b/c")
        );
    }

    @Test
    public void uploadFolder_noFilesSpecified_createsEmptyFolder() {
        folderService.createFolder(USER_ID, "a/b/c");
        folderService.uploadFolder(USER_ID, new MultipartFile[]{}, "a/b/c");
        Assertions.assertTrue(minioRepository.isObjectExists(USER_ID_DIRECTORY + "a/b/c/"));
    }

    @Test
    public void uploadFolder_intoExistingPath_savesFilesRelativelyAtPath() {
        folderService.createFolder(USER_ID, "a/b");
        folderService.uploadFolder(USER_ID, new MultipartFile[]{getMockFile()}, "a/b");
        String minioPath = MinioUtils.getMinioPath(USER_ID, "a/b/" + MOCK_FILE_NAME);
        Assertions.assertTrue(minioRepository.isObjectExists(minioPath));
    }

    @Test
    public void uploadFolder_resourceWithNameAlreadyExists_throwsResourceAlreadyExistsException() {
        folderService.uploadFolder(USER_ID, new MultipartFile[]{getMockFile()}, "/");
        Assertions.assertThrows(
                ResourceAlreadyExistsException.class,
                () -> folderService.uploadFolder(USER_ID, new MultipartFile[]{getMockFile()}, "/")
        );
    }

    @Test
    public void createFolder_isFolder_notFile() {
        folderService.createFolder(USER_ID, "a");
        Assertions.assertTrue(folderService.isFolderExists(USER_ID, "a"));
        Assertions.assertFalse(fileService.isFileExists(USER_ID, "a"));
    }

    @Test
    public void renameFolder_emptyFolder_folderExists_createsNewFolder_deletesOldFolder() {
        folderService.createFolder(USER_ID, "a");
        folderService.renameFolder(USER_ID, "a", "b");
        Assertions.assertTrue(folderService.isFolderExists(USER_ID, "b"));
        Assertions.assertFalse(folderService.isFolderExists(USER_ID, "a"));
    }

    @Test
    public void renameFolder_nonEmptyFolder_folderExists_copiesContents_deletesOldContents() {
        folderService.createFolder(USER_ID, "a");
        folderService.uploadFolder(USER_ID, new MultipartFile[] { getMockFile() }, "a");
        folderService.renameFolder(USER_ID, "a", "b");

        String objectInB = MinioUtils.getMinioPath(USER_ID, "b/" + MOCK_FILE_NAME);
        String objectInA = MinioUtils.getMinioPath(USER_ID, "a/" + MOCK_FILE_NAME);
        Assertions.assertTrue(minioRepository.isObjectExists(objectInB));
        Assertions.assertFalse(minioRepository.isObjectExists(objectInA));
        Assertions.assertFalse(folderService.isFolderExists(USER_ID, "a"));
    }

    @Test
    public void renameFolder_folderWithThisNameAlreadyExists_throwsResourceAlreadyExistsException() {
        folderService.createFolder(USER_ID, "a");
        folderService.uploadFolder(USER_ID, new MultipartFile[] { getMockFile() }, "a");
        folderService.createFolder(USER_ID, "b");
        folderService.uploadFolder(USER_ID, new MultipartFile[] { getMockFile() }, "b");
        Assertions.assertThrows(
                ResourceAlreadyExistsException.class,
                () -> folderService.renameFolder(USER_ID, "a", "b")
        );
    }

    @Test
    public void renameFolder_folderNotExists_throwsFolderNotFoundException() {
        Assertions.assertThrows(
                FolderNotFoundException.class,
                () -> folderService.renameFolder(USER_ID, "a", "b")
        );
    }

    @Test
    public void renameFolder_toSameName_returnsOldPath() {
        folderService.createFolder(USER_ID, "a");
        Assertions.assertEquals(
                "a",
                folderService.renameFolder(USER_ID, "a", "a")
        );
    }

    @Test
    public void deleteFolder_folderExists_onlyUserHomeFolderLeft() {
        folderService.createFolder(USER_ID, "a");
        folderService.uploadFolder(USER_ID, new MultipartFile[] { getMockFile() }, "a");
        folderService.deleteFolder(USER_ID, "a");
        List<MinioObjectDto> leftObjects = minioRepository.listObjects("", true);
        Assertions.assertEquals(1, leftObjects.size());
        Assertions.assertEquals(USER_ID_DIRECTORY, leftObjects.get(0).getName());
    }

    @Test
    public void deleteFolder_folderNotExists_throwsFolderNotFoundException() {
        Assertions.assertThrows(
                FolderNotFoundException.class,
                () -> folderService.deleteFolder(USER_ID, "a")
        );
    }

    @Test
    public void getFolderContent_folderWithSingleFile_returnsSingleFile() {
        folderService.createFolder(USER_ID, "a");
        folderService.uploadFolder(USER_ID, new MultipartFile[]{ getMockFile() }, "a");
        List<FileDto> files = folderService.getFolderContent(USER_ID, "a", ALL_ITEMS).toList();
        Assertions.assertEquals(1, files.size());
        Assertions.assertEquals(MOCK_FILE_NAME, files.get(0).getName());
        Assertions.assertEquals("a/" + MOCK_FILE_NAME, files.get(0).getPath());
        Assertions.assertFalse(files.get(0).isFolder());
    }

    @Test
    public void getFolderContent_folderWithFolder_returnsFolder() {
        folderService.createFolder(USER_ID, "a/b");
        folderService.uploadFolder(USER_ID, new MultipartFile[]{ getMockFile() }, "a/b");
        List<FileDto> files = folderService.getFolderContent(USER_ID, "a", ALL_ITEMS).toList();
        Assertions.assertEquals(1, files.size());
        Assertions.assertEquals("b", files.get(0).getName());
        Assertions.assertEquals("a/b/", files.get(0).getPath());
        Assertions.assertTrue(files.get(0).isFolder());
    }

    @Test
    public void getFolderContent_folderNotExists_throwsFolderNotFoundException() {
        Assertions.assertThrows(
                FolderNotFoundException.class,
                () -> folderService.getFolderContent(USER_ID, "a", ALL_ITEMS)
        );
    }

    @SneakyThrows
    @Test
    public void downloadFolder_folderExists_returnsFolderZip() {
        folderService.createFolder(USER_ID, "a");
        folderService.uploadFolder(USER_ID, new MultipartFile[] { getMockFile() }, "a");
        byte[] folderContents = folderService.downloadFolder(USER_ID, "a");
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(folderContents))) {
            zip.getNextEntry();

            ByteArrayOutputStream decompressedFileContents = new ByteArrayOutputStream();
            zip.transferTo(decompressedFileContents);
            Assertions.assertArrayEquals(MOCK_FILE_CONTENTS, decompressedFileContents.toByteArray());

            Assertions.assertNull(zip.getNextEntry());
        }
    }

    @Test
    public void downloadFolder_folderNotExists_throwsFolderNotFoundException() {
        Assertions.assertThrows(
                FolderNotFoundException.class,
                () -> folderService.downloadFolder(USER_ID, "a")
        );
    }

    @Test
    public void isFolderExists_folderExists_returnsTrue() {
        folderService.createFolder(USER_ID, "a");
        folderService.uploadFolder(USER_ID, new MultipartFile[] { getMockFile() }, "a");
        Assertions.assertTrue(folderService.isFolderExists(USER_ID, "a"));
    }

    @Test
    public void isFolderExists_folderNotExists_returnsFalse() {
        Assertions.assertFalse(folderService.isFolderExists(USER_ID, "a"));
    }

    private static final long USER_ID = 1;
    private static final String USER_ID_DIRECTORY = "user-%d-files/".formatted(USER_ID);
    private static final byte[] MOCK_FILE_CONTENTS = "Mock file contents".getBytes(StandardCharsets.UTF_8);
    private static final String MOCK_FILE_ORIGINAL_FILENAME = "Mock file name";
    private static final String MOCK_FILE_NAME = MOCK_FILE_ORIGINAL_FILENAME;
    private static final String MOCK_FILE_CONTENT_TYPE = "";
    private static final PageRequest ALL_ITEMS = PageRequest.ofSize(Integer.MAX_VALUE);

    private MultipartFile getMockFile() {
        return new MockMultipartFile(
                MOCK_FILE_NAME,
                MOCK_FILE_ORIGINAL_FILENAME,
                MOCK_FILE_CONTENT_TYPE,
                MOCK_FILE_CONTENTS
        );
    }

    @Container
    private static final MinIOContainer minioContainer = TestUtils.createMinIOContainer();

    @DynamicPropertySource
    public static void minioProperties(DynamicPropertyRegistry registry) {
        TestUtils.addMinioProperties(registry, minioContainer);
    }

}
