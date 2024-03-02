package com.vadimistar.cloudfilestorage.services.impl;

import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.services.FolderService;
import com.vadimistar.cloudfilestorage.services.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final FolderService folderService;

    @Override
    public Stream<FileDto> searchFiles(long userId, String query) {
        return folderService.getAllContent(userId)
                .filter(file -> file.getName().contains(query));
    }
}
