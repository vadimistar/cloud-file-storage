package com.vadimistar.cloudfilestorage.minio.repository;

import com.vadimistar.cloudfilestorage.minio.dto.ListObjectsResponseDto;

import java.io.InputStream;
import java.util.stream.Stream;

public interface MinioRepository {
    Stream<ListObjectsResponseDto> listObjects(String prefix, boolean recursive);
    void copyObject(String from, String to);
    void removeObject(String object);
    void removeObjects(String prefix);
    void putObject(String object, InputStream inputStream, long size);
    InputStream getObject(String object);
    boolean isObjectExists(String object);
    void makeBucket();
    boolean isBucketExists();
    void removeBucket();
}
