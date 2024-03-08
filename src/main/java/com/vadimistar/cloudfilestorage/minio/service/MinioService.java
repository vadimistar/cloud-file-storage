package com.vadimistar.cloudfilestorage.minio.service;

import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;

import java.io.InputStream;
import java.util.stream.Stream;

public interface MinioService {
    boolean isFileExists(String object);
    boolean isFolderExists(String object);
    void putObject(String object, InputStream inputStream, long size);
    void copyObject(String from, String to);
    void removeObject(String object);
    InputStream getObject(String object);
    Stream<ListObjectsResponseDto> listObjects(String prefix, boolean recursive);
}
