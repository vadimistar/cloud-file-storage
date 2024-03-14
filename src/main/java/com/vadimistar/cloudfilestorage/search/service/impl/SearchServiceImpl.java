package com.vadimistar.cloudfilestorage.search.service.impl;

import com.vadimistar.cloudfilestorage.folder.service.FolderService;
import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import com.vadimistar.cloudfilestorage.search.mapper.FoundFileMapper;
import com.vadimistar.cloudfilestorage.search.service.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final FolderService folderService;

    @Override
    public Stream<FoundFileDto> searchFiles(long userId, String query) {
        return folderService.getAllContent(userId)
                .filter(file -> file.getName().contains(query))
                .map(FoundFileMapper::makeFoundFileDto);
    }
}
