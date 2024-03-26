package com.vadimistar.cloudfilestorage.search.service.impl;

import com.vadimistar.cloudfilestorage.folder.service.FolderService;
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

    private final FolderService folderService;
    private final FoundFileMapper foundFileMapper;

    @Override
    public Page<FoundFileDto> searchFiles(long userId, String query, Pageable pageable) {
        int start = pageable.getPageNumber() * pageable.getPageSize();
        List<FoundFileDto> foundFiles = folderService.getAllContent(userId).stream()
                .filter(file -> file.getName().contains(query))
                .skip(start)
                .limit(pageable.getPageSize())
                .map(foundFileMapper::makeFoundFileDto)
                .toList();
        return new PageImpl<>(foundFiles,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                foundFiles.size());
    }
}
