package com.vadimistar.cloudfilestorage.services.impl;

import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.exceptions.SearchServiceException;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final FileService fileService;

    @Override
    public List<FileDto> searchFiles(long userId, String query) throws SearchServiceException {
        try {
            return fileService.getAllFiles(userId)
                    .stream()
                    .filter(file -> file.getName().contains(query))
                    .toList();
        } catch (FileServiceException e) {
            throw new SearchServiceException(e.getMessage());
        }
    }
}
