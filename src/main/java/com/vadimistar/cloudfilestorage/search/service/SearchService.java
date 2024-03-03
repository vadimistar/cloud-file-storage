package com.vadimistar.cloudfilestorage.search.service;

import com.vadimistar.cloudfilestorage.common.dto.FileDto;

import java.util.stream.Stream;

public interface SearchService {

    Stream<FileDto> searchFiles(long userId, String query);
}
