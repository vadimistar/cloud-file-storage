package com.vadimistar.cloudfilestorage.services;

import com.vadimistar.cloudfilestorage.dto.FileDto;

import java.util.stream.Stream;

public interface SearchService {

    Stream<FileDto> searchFiles(long userId, String query);
}
