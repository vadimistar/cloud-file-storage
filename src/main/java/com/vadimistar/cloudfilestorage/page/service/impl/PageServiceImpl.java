package com.vadimistar.cloudfilestorage.page.service.impl;

import com.vadimistar.cloudfilestorage.common.exceptions.InvalidPageException;
import com.vadimistar.cloudfilestorage.page.dto.PaginationItemDto;
import com.vadimistar.cloudfilestorage.page.dto.PaginationItemState;
import com.vadimistar.cloudfilestorage.page.service.PageService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class PageServiceImpl implements PageService {


    @Override
    public <T> Stream<T> getPage(Stream<T> stream, int page, int pageSize) {
        if (page < 1) {
            throw new InvalidPageException("Invalid page: " + page);
        }
        int index = page - 1;
        return stream.skip((long) index * pageSize).limit(pageSize);
    }

    @Override
    public int countPages(int pageSize, int totalItems) {
        return (totalItems / pageSize) + (totalItems % pageSize != 0 ? 1 : 0);
    }

    @Override
    public List<PaginationItemDto> createPagination(int page, int totalPages, String uri) {
        List<PaginationItemDto> result = new ArrayList<>();

        if (page > FIRST_PAGE) {
            int previousPage = page - 1;
            result.add(new PaginationItemDto(
                    getUriForPage(uri, previousPage), "Previous", PaginationItemState.INACTIVE
            ));
        } else {
            result.add(new PaginationItemDto(
                    getUriForPage(uri, page), "Previous", PaginationItemState.DISABLED
            ));
        }

        for (int i = 1; i <= totalPages; i ++) {
            PaginationItemState state = PaginationItemState.INACTIVE;
            if (i == page) {
                state = PaginationItemState.ACTIVE;
            }

            result.add(new PaginationItemDto(
                    getUriForPage(uri, i), Integer.toString(i), state
            ));
        }

        if (page < totalPages) {
            int nextPage = page + 1;
            result.add(new PaginationItemDto(
                    getUriForPage(uri, nextPage), "Next", PaginationItemState.INACTIVE
            ));
        } else {
            result.add(new PaginationItemDto(
                    getUriForPage(uri, page), "Next", PaginationItemState.DISABLED
            ));
        }

        return result;
    }

    @SneakyThrows
    private static String getUriForPage(String uri, int page) {
        return UriComponentsBuilder.fromUriString(uri)
                .replaceQueryParam("page", page)
                .build(false)
                .toUriString();
    }

    private static final int FIRST_PAGE = 1;
}
