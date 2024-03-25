package com.vadimistar.cloudfilestorage.search.service;

import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;

import java.util.List;

public interface SearchService {

    List<FoundFileDto> searchFiles(long userId, String query);
}
