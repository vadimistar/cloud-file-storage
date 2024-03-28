package com.vadimistar.cloudfilestorage.search.service.impl;

import com.vadimistar.cloudfilestorage.common.utils.MinioUtils;
import com.vadimistar.cloudfilestorage.minio.service.MinioService;
import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import com.vadimistar.cloudfilestorage.search.mapper.FoundFileMapper;
import com.vadimistar.cloudfilestorage.search.service.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final MinioService minioService;
    private final FoundFileMapper foundFileMapper;

    @Override
    public Page<FoundFileDto> searchFiles(long userId, String query, Pageable pageable) {
        int start = pageable.getPageNumber() * pageable.getPageSize();
        List<FoundFileDto> foundFiles = minioService.listObjects(MinioUtils.getMinioPath(userId, "/"), true).stream()
                .map(foundFileMapper::makeFoundFileDto)
                .filter(file -> file.getName().contains(query))
                .skip(start)
                .limit(pageable.getPageSize())
                .toList();
        return new PageImpl<>(foundFiles,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                foundFiles.size());
    }
}
