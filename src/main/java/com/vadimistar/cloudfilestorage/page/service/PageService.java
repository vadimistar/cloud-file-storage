package com.vadimistar.cloudfilestorage.page.service;

import com.vadimistar.cloudfilestorage.page.dto.PaginationItemDto;

import java.util.List;
import java.util.stream.Stream;

public interface PageService {

    <T> Stream<T> getPage(Stream<T> stream, int page, int pageSize);
    int countPages(int pageSize, int totalItems);
    List<PaginationItemDto> createPagination(int page, int totalPages, String uri);
}
