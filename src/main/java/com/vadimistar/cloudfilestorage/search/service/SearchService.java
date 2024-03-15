package com.vadimistar.cloudfilestorage.search.service;

import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;

import java.util.stream.Stream;

public interface SearchService {

    Stream<FoundFileDto> searchFiles(long userId, String query);
}
