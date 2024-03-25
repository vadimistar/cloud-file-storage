package com.vadimistar.cloudfilestorage.search.service;

import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchService {

    List<FoundFileDto> searchFiles(long userId, String query);

    Page<FoundFileDto> searchFiles(long userId, String query, Pageable pageable);
}
