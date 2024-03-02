package com.vadimistar.cloudfilestorage.services.impl;

import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.exceptions.FolderAlreadyExistsException;
import com.vadimistar.cloudfilestorage.exceptions.UploadFileException;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.FolderService;
import com.vadimistar.cloudfilestorage.utils.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FolderServiceImpl extends MinioService implements FolderService {

    private final FileService fileService;

    public FolderServiceImpl(Minio minio, FileService fileService) {
        super(minio);
        this.fileService = fileService;
    }

    @Override
    public void createFolder(long userId, String path) {
        path = PathUtils.makeDirectoryPath(path);
        validateResourceNotExists(userId, path);
        fileService.uploadFile(userId, getFolderObjectContent(), FOLDER_OBJECT_SIZE, path);
    }

    @Override
    public void uploadFolder(long userId, MultipartFile[] files, String path) {
        Set<String> fileDirectories = new HashSet<>();

        for (MultipartFile file : files) {
            if (file.getSize() == 0) {
                throw new UploadFileException("Unable to upload files with size equal to 0");
            }

            String filePath = PathUtils.join(path, file.getOriginalFilename());
            String parentDirectory = PathUtils.getParentDirectory(file.getOriginalFilename());
            if (!parentDirectory.isEmpty()) {
                fileDirectories.add(PathUtils.join(path, parentDirectory));
            }

            try {
                fileService.uploadFile(userId, file.getInputStream(), file.getSize(), filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        createSubdirectories(userId, fileDirectories);
    }

    @Override
    public String renameFolder(long userId, String path, String name) {
        if (PathUtils.getFilename(path).equals(name)) {
            return path;
        }

        path = PathUtils.makeDirectoryPath(path);
        String parentDirectory = PathUtils.getParentDirectory(path);
        String newPath = PathUtils.join(parentDirectory, name);

        validateResourceNotExists(userId, newPath);

        String minioPath = MinioUtils.getMinioPath(userId, path);

        List<String> files = new ArrayList<>();
        List<String> directories = new ArrayList<>();

        minio.listObjects(minioPath, ListObjectsMode.RECURSIVE).forEach(item -> {
            if (MinioUtils.isDirectory(item)) {
                directories.add(item.objectName());
            } else {
                files.add(item.objectName());
            }
        });

        renameFilesInFolder(files, path, PathUtils.makeDirectoryPath(newPath));
        renameDirectoriesInFolder(directories, PathUtils.getFilename(path), name);

        return newPath;
    }

    @Override
    public void deleteFolder(long userId, String path) {
        path = PathUtils.makeDirectoryPath(path);
        minio.listObjects(MinioUtils.getMinioPath(userId, path), ListObjectsMode.RECURSIVE)
                .forEach(item -> minio.removeObject(item.objectName()));
    }

    @Override
    public Stream<FileDto> getFolderContent(long userId, String path) {
        return listFiles(userId, path, ListObjectsMode.NON_RECURSIVE);
    }

    @Override
    public Stream<FileDto> getAllContent(long userId) {
        return listFiles(userId, "/", ListObjectsMode.RECURSIVE);
    }

    @Override
    public byte[] downloadFolder(long userId, String path) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(result)) {
            listFiles(userId, path, ListObjectsMode.RECURSIVE)
                    .filter(file -> !file.isFolder())
                    .forEach(file -> downloadFile(file, userId, path, zipOutputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toByteArray();
    }

    private InputStream getFolderObjectContent() {
        return new ByteArrayInputStream(new byte[] {});
    }

    private static final long FOLDER_OBJECT_SIZE = 0;

    private void createSubdirectories(long userId, Iterable<String> fileDirectories) {
        Set<String> subdirectories = new HashSet<>();

        for (String directory : fileDirectories) {
            subdirectories.addAll(PathUtils.getSubdirectories(directory));
            subdirectories.add(directory);
        }

        for (String subdirectory : subdirectories) {
            validateResourceNotExists(userId, subdirectory);
            createFolder(userId, subdirectory);
        }
    }

    private void renameFilesInFolder(List<String> files, String oldPath, String newPath) {
        files.forEach(file -> {
            minio.copyObject(
                    file,
                    file.replaceFirst(oldPath, newPath)
            );
            minio.removeObject(file);
        });
    }

    private void renameDirectoriesInFolder(List<String> directories, String oldName, String newName) {
        directories.forEach(directory -> {
            createFolder(directory.replaceFirst(
                    Pattern.quote(oldName), Matcher.quoteReplacement(newName)
            ));
            minio.removeObject(directory);
        });
    }

    private Stream<FileDto> listFiles(long userId, String path, ListObjectsMode mode) {
        path = PathUtils.makeDirectoryPath(path);
        String prefix = MinioUtils.getMinioPath(userId, path);
        return minio.listObjects(prefix, mode)
                .filter(item -> !item.objectName().equals(prefix))
                .map(FileDtoMapper::makeFileDto);
    }

    private void downloadFile(FileDto file, long userId, String path, ZipOutputStream zipOutputStream) {
        String relativePath = PathUtils.getRelativePath(file.getPath(), path);

        ByteArrayResource byteArrayResource = new ByteArrayResource(
                fileService.downloadFile(userId, file.getPath())
        );

        ZipEntry zipEntry = new ZipEntry(relativePath);
        zipEntry.setSize(byteArrayResource.contentLength());
        zipEntry.setTime(System.currentTimeMillis());
        try {
            zipOutputStream.putNextEntry(zipEntry);
            StreamUtils.copy(byteArrayResource.getInputStream(), zipOutputStream);
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void createFolder(String object) {
        minio.putObject(object, getFolderObjectContent(), FOLDER_OBJECT_SIZE);
    }

}
