package com.vadimistar.cloudfilestorage.search.service;

import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {

    Page<FoundFileDto> searchFiles(long userId, String query, Pageable pageable);
}
