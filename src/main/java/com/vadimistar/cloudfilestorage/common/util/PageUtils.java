package com.vadimistar.cloudfilestorage.common.util;

import com.vadimistar.cloudfilestorage.common.dto.PaginationItemDto;
import com.vadimistar.cloudfilestorage.common.exceptions.InvalidPageException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.parameters.P;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@UtilityClass
public class PageUtils {

    // Page number starts from 1: first page is 1, second page is 2, etc.
    public static <T> Stream<T> getPage(Stream<T> stream, int pageNumber, int pageSize) {
        long pageIndex = pageNumber - 1;
        if (pageIndex < 0) {
            throw new InvalidPageException("Invalid page number: " + pageNumber);
        }
        return stream.skip(pageIndex * pageSize).limit(pageSize);
    }

    public static int countPages(int pageSize, int totalItems) {
        return (totalItems / pageSize) + ((totalItems % pageSize == 0) ? 0 : 1);
    }

    public static List<PaginationItemDto> createPagination(int page, int totalPages, String uri) {
        List<PaginationItemDto> result = new ArrayList<>();

        if (page > 1) {
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
}
