package com.vadimistar.cloudfilestorage.folder.service.impl;

import com.vadimistar.cloudfilestorage.common.exception.FolderNotFoundException;
import com.vadimistar.cloudfilestorage.common.mapper.FileDtoMapper;
import com.vadimistar.cloudfilestorage.minio.service.MinioService;
import com.vadimistar.cloudfilestorage.minio.utils.MinioUtils;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.common.exception.UploadFileException;
import com.vadimistar.cloudfilestorage.common.util.StringUtils;
import com.vadimistar.cloudfilestorage.folder.service.FolderService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
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
@AllArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final MinioService minioService;

    @Override
    public void createFolder(long userId, String path) {
        path = PathUtils.makeDirectoryPath(path);
        MinioUtils.validateResourceNotExists(minioService, userId, path);
        minioService.putObject(MinioUtils.getMinioPath(userId, path), getFolderObjectContent(), FOLDER_OBJECT_SIZE);
    }

    @Override
    public synchronized void uploadFolder(long userId, MultipartFile[] files, String path) {
        path = PathUtils.makeDirectoryPath(path);
        Set<String> fileDirectories = new HashSet<>();

        for (MultipartFile file : files) {
            if (file.getSize() == 0) {
                throw new UploadFileException("Unable to upload files with size equal to 0", path);
            }

            String parentDirectory = PathUtils.getParentDirectory(file.getOriginalFilename());
            if (!parentDirectory.isEmpty()) {
                parentDirectory = PathUtils.makeDirectoryPath(parentDirectory);
                fileDirectories.add(PathUtils.join(path, parentDirectory));
            }
        }

        createSubdirectories(userId, fileDirectories);

        for (MultipartFile file : files) {
            String filePath = PathUtils.join(path, file.getOriginalFilename());
            try {
                MinioUtils.validateResourceNotExists(minioService, userId, filePath);
                minioService.putObject(MinioUtils.getMinioPath(userId, filePath), file.getInputStream(), file.getSize());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public synchronized String renameFolder(long userId, String path, String name) {
        validateFolderExists(userId, path);

        if (PathUtils.getFilename(path).equals(name)) {
            return path;
        }

        path = PathUtils.makeDirectoryPath(path);
        String parentDirectory = PathUtils.getParentDirectory(path);
        String newPath = PathUtils.join(parentDirectory, name);

        MinioUtils.validateResourceNotExists(minioService, userId, newPath);

        List<String> files = new ArrayList<>();
        List<String> directories = new ArrayList<>();

        minioService.listObjects(MinioUtils.getMinioPath(userId, path), true).forEach(item -> {
            if (item.isDirectory()) {
                directories.add(item.getName());
            } else {
                files.add(item.getName());
            }
        });

        renameFilesInFolder(files, path, PathUtils.makeDirectoryPath(newPath));
        renameDirectoriesInFolder(directories, PathUtils.getFilename(path), name);

        return newPath;
    }

    @Override
    public void deleteFolder(long userId, String path) {
        validateFolderExists(userId, path);
        path = PathUtils.makeDirectoryPath(path);
        minioService.listObjects(MinioUtils.getMinioPath(userId, path), true)
                .forEach(item -> minioService.removeObject(item.getName()));
    }

    @Override
    public Stream<FileDto> getFolderContent(long userId, String path) {
        validateFolderExists(userId, path);
        return listFiles(userId, path, false);
    }

    @Override
    public Stream<FileDto> getAllContent(long userId) {
        return listFiles(userId, "/", true);
    }

    @Override
    public byte[] downloadFolder(long userId, String path) {
        validateFolderExists(userId, path);
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(result)) {
            listFiles(userId, path, true)
                    .filter(file -> !file.isFolder())
                    .forEach(file -> downloadFile(file, userId, path, zipOutputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toByteArray();
    }

    @Override
    public boolean isFolderExists(long userId, String path) {
        return minioService.isFolderExists(MinioUtils.getMinioPath(userId, path));
    }

    private InputStream getFolderObjectContent() {
        return new ByteArrayInputStream(new byte[] {});
    }

    private static final long FOLDER_OBJECT_SIZE = 0;

    private void createSubdirectories(long userId, Iterable<String> fileDirectories) {
        SortedSet<String> subdirectories = new TreeSet<>((dir1, dir2) -> {
            long depth1 = StringUtils.count(dir1, '/');
            long depth2 = StringUtils.count(dir2, '/');
            return Long.compare(depth1, depth2);
        });

        for (String directory : fileDirectories) {
            subdirectories.addAll(PathUtils.getSubdirectories(directory));
            subdirectories.add(directory);
        }

        for (String subdirectory : subdirectories) {
            createFolder(userId, subdirectory);
        }
    }

    private void renameFilesInFolder(List<String> files, String oldPath, String newPath) {
        files.forEach(file -> {
            minioService.copyObject(
                    file,
                    file.replaceFirst(oldPath, newPath)
            );
            minioService.removeObject(file);
        });
    }

    private void renameDirectoriesInFolder(List<String> directories, String oldName, String newName) {
        directories.forEach(directory -> {
            createFolder(directory.replaceFirst(
                    Pattern.quote(oldName), Matcher.quoteReplacement(newName)
            ));
            minioService.removeObject(directory);
        });
    }

    private Stream<FileDto> listFiles(long userId, String path, boolean recursive) {
        path = PathUtils.makeDirectoryPath(path);
        String prefix = MinioUtils.getMinioPath(userId, path);
        return minioService.listObjects(prefix, recursive)
                .filter(item -> !item.getName().equals(prefix))
                .map(FileDtoMapper::makeFileDto);
    }

    @SneakyThrows
    private void downloadFile(FileDto file, long userId, String path, ZipOutputStream zipOutputStream) {
        String relativePath = PathUtils.getRelativePath(file.getPath(), path);

        ByteArrayResource byteArrayResource = new ByteArrayResource(
                minioService
                        .getObject(MinioUtils.getMinioPath(userId, file.getPath()))
                        .readAllBytes()
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
        minioService.putObject(object, getFolderObjectContent(), FOLDER_OBJECT_SIZE);
    }

    private void validateFolderExists(long userId, String path) {
        if (!isFolderExists(userId, path)) {
            throw new FolderNotFoundException("Folder is not found: " + path);
        }
    }
}
