package com.vadimistar.cloudfilestorage.services;

import com.vadimistar.cloudfilestorage.exceptions.BucketServiceException;

public interface BucketService {

    void createBucket(String name) throws BucketServiceException;

    boolean isBucketExists(String name) throws BucketServiceException;
}
