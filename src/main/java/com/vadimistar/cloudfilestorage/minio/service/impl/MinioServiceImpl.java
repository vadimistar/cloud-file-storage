package com.vadimistar.cloudfilestorage.minio.service.impl;

import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;
import com.vadimistar.cloudfilestorage.minio.repository.ListObjectsMode;
import com.vadimistar.cloudfilestorage.minio.repository.MinioRepository;
import com.vadimistar.cloudfilestorage.minio.service.MinioService;
import com.vadimistar.cloudfilestorage.minio.utils.MinioUtils;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioRepository minioRepository;

    @Override
    public boolean isFileExists(String object) {
        return minioRepository.isObjectExists(object);
    }

    @Override
    public boolean isFolderExists(String object) {
        object = PathUtils.makeDirectoryPath(object);
        return minioRepository.listObjects(object, ListObjectsMode.NON_RECURSIVE).findAny().isPresent();
    }

    @Override
    public void putObject(String object, InputStream inputStream, long size) {
        minioRepository.putObject(object, inputStream, size);
    }

    @Override
    public void copyObject(String from, String to) {
        minioRepository.copyObject(from, to);
    }

    @Override
    public void removeObject(String object) {
        minioRepository.removeObject(object);
    }

    @Override
    public InputStream getObject(String object) {
        return minioRepository.getObject(object);
    }

    @Override
    public Stream<ListObjectsResponseDto> listObjects(String prefix, boolean recursive) {
        return minioRepository.listObjects(prefix, recursive ? ListObjectsMode.RECURSIVE : ListObjectsMode.NON_RECURSIVE);
    }
}
