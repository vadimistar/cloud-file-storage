package com.vadimistar.cloudfilestorage.folder.service.impl;

import com.vadimistar.cloudfilestorage.common.mapper.FileMapper;
import com.vadimistar.cloudfilestorage.folder.exception.FolderNotFoundException;
import com.vadimistar.cloudfilestorage.folder.exception.UploadFolderException;
import com.vadimistar.cloudfilestorage.folder.service.FolderService;
import com.vadimistar.cloudfilestorage.minio.service.MinioService;
import com.vadimistar.cloudfilestorage.minio.util.MinioUtils;
import com.vadimistar.cloudfilestorage.common.util.path.PathDepthComparator;
import com.vadimistar.cloudfilestorage.common.util.path.PathUtils;
import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
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
    private final FileMapper fileMapper;
    private final PathDepthComparator pathDepthComparator;

    @Override
    public void createFolder(long userId, String path) {
        MinioUtils.validateResourceNotExists(minioService, userId, path);
        path = PathUtils.makeDirectoryPath(path);
        minioService.putObject(MinioUtils.getMinioPath(userId, path), getFolderObjectContent(), FOLDER_OBJECT_SIZE);
    }

    @Override
    public synchronized void uploadFolder(long userId, MultipartFile[] files, String path) {
        path = PathUtils.makeDirectoryPath(path);
        Collection<String> fileDirectories = getFileDirectories(files, path);
        createSubdirectories(userId, fileDirectories);
        for (MultipartFile file : files) {
            uploadFile(userId, path, file);
        }
    }

    @Override
    public synchronized String renameFolder(long userId, String path, String name) {
        validateFolderExists(userId, path);
        String minioOldFilename = MinioUtils.getMinioFilename(path);
        String minioNewFilename = MinioUtils.getMinioFilename(name);
        if (minioOldFilename.equals(minioNewFilename)) {
            return path;
        }

        path = PathUtils.makeDirectoryPath(path);
        String newPath = createNewPath(path, name);

        MinioUtils.validateResourceNotExists(minioService, userId, newPath);

        List<String> files = new ArrayList<>();
        List<String> directories = new ArrayList<>();
        extractFilesAndDirectories(userId, path, files, directories);
        renameContentsInFolder(userId, path, newPath, files, directories);

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
            List<FileDto> filesToDownload = getFilesToDownload(userId, path);
            filesToDownload.forEach(file -> downloadFile(file, userId, path, zipOutputStream));
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

    private Collection<String> getFileDirectories(MultipartFile[] files, String path) {
        Set<String> fileDirectories = new HashSet<>();
        for (MultipartFile file : files) {
            if (file.getSize() == 0) {
                throw new UploadFolderException("Unable to upload files with size equal to 0", path);
            }
            String parentDirectory = PathUtils.getParentDirectory(file.getOriginalFilename());
            if (!parentDirectory.isEmpty()) {
                parentDirectory = PathUtils.makeDirectoryPath(parentDirectory);
                fileDirectories.add(PathUtils.join(path, parentDirectory));
            }
        }
        return fileDirectories;
    }

    private void createSubdirectories(long userId, Iterable<String> fileDirectories) {
        SortedSet<String> subdirectories = new TreeSet<>(pathDepthComparator);
        for (String directory : fileDirectories) {
            subdirectories.addAll(PathUtils.getSubdirectories(directory));
            subdirectories.add(directory);
        }
        for (String subdirectory : subdirectories) {
            createFolder(userId, subdirectory);
        }
    }

    private void uploadFile(long userId, String path, MultipartFile file) {
        String filePath = PathUtils.join(path, file.getOriginalFilename());
        try {
            MinioUtils.validateResourceNotExists(minioService, userId, filePath);
            minioService.putObject(MinioUtils.getMinioPath(userId, filePath), file.getInputStream(), file.getSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String createNewPath(String path, String name) {
        String parentDirectory = PathUtils.getParentDirectory(path);
        return PathUtils.join(parentDirectory, name);
    }

    private void extractFilesAndDirectories(long userId, String path, List<String> files, List<String> directories) {
        String minioPath = MinioUtils.getMinioPath(userId, path);
        minioService.listObjects(minioPath, true).forEach(item -> {
            if (item.isDirectory()) {
                directories.add(item.getName());
            } else {
                files.add(item.getName());
            }
        });
    }

    private void renameContentsInFolder(long userId, String oldPath, String newPath, List<String> files, List<String> directories) {
        newPath = PathUtils.makeDirectoryPath(newPath);
        String minioOldPath = MinioUtils.getMinioPath(userId, oldPath);
        String minioNewPath = MinioUtils.getMinioPath(userId, newPath);
        renameFilesInFolder(files, minioOldPath, minioNewPath);
        renameDirectoriesInFolder(directories, minioOldPath, minioNewPath);
    }

    private void renameFilesInFolder(List<String> files, String oldPath, String newPath) {
        files.forEach(file -> {
            minioService.copyObject(
                    file,
                    file.replaceFirst(Pattern.quote(oldPath), Matcher.quoteReplacement(newPath))
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
                .map(fileMapper::makeFileDto);
    }

    private List<FileDto> getFilesToDownload(long userId, String path) {
        List<FileDto> notFolderFiles = listFiles(userId, path, true)
                .filter(file -> !file.isFolder())
                .toList();
        if (notFolderFiles.isEmpty()) {
            return listFiles(userId, path, true).toList();
        } else {
            return notFolderFiles;
        }
    }

    private void downloadFile(FileDto file, long userId, String path, ZipOutputStream zipOutputStream) {
        String relativePath = PathUtils.getRelativePath(file.getPath(), path);

        try {
            ByteArrayResource byteArrayResource = new ByteArrayResource(
                    minioService
                            .getObject(MinioUtils.getMinioPath(userId, file.getPath()))
                            .readAllBytes()
            );

            ZipEntry zipEntry = new ZipEntry(relativePath);
            zipEntry.setSize(byteArrayResource.contentLength());
            zipEntry.setTime(System.currentTimeMillis());

            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(byteArrayResource.getByteArray());
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
