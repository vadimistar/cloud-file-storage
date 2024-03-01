package com.vadimistar.cloudfilestorage.services;

import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.exceptions.SearchServiceException;

import java.util.List;

public interface SearchService {

    List<FileDto> searchFiles(long userId, String query) throws SearchServiceException;
}
