package com.vadimistar.cloudfilestorage.adapters.minio.exceptions;

public class MinioException extends RuntimeException {

    public MinioException(String message) {
        super(message);
    }

    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }
}
