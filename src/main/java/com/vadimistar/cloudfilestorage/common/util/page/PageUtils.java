package com.vadimistar.cloudfilestorage.common.util.page;

import com.vadimistar.cloudfilestorage.common.exception.InvalidPageException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@UtilityClass
public class PageUtils {

    public static <T> List<T> getPage(Collection<T> allContent, int pageSize, int page) {
        if (page < 1 || page > getTotalPages(allContent.size(), pageSize)) {
            throw new InvalidPageException("Invalid page: " + page);
        }
        int index = page - 1;
        return allContent.stream().skip((long) index * pageSize).limit(pageSize).toList();
    }

    public static List<PageButtonDto> createPageButtons(
            int allContentSize, int pageSize, int currentPage, HttpServletRequest request) {
        String uri = getUriWithQuery(request);
        int totalPages = getTotalPages(allContentSize, pageSize);
        List<PageButtonDto> result = new ArrayList<>();
        result.add(createPreviousButton(currentPage, uri));
        result.addAll(createNumberButtons(currentPage, totalPages, uri));
        result.add(createNextButton(currentPage, totalPages, uri));
        return result;
    }

    private static int getTotalPages(int allContentSize, int pageSize) {
        if (allContentSize == 0) {
            return 1;
        }
        return (int) Math.ceil((double) allContentSize / pageSize);
    }

    private static PageButtonDto createPreviousButton(int page, String uri) {
        if (page > 1) {
            int previousPage = page - 1;
            return PageButtonDto.builder()
                    .uri(getUri(uri, previousPage))
                    .text("Previous")
                    .state(PageButtonState.INACTIVE)
                    .build();
        }
        return PageButtonDto.builder()
                .text("Previous")
                .state(PageButtonState.DISABLED)
                .build();
    }

    private static List<PageButtonDto> createNumberButtons(int page, int totalPages, String uri) {
        ArrayList<PageButtonDto> buttons = new ArrayList<>();
        buttons.ensureCapacity(totalPages);
        for (int i = 1; i <= totalPages; i ++) {
            PageButtonState state = (i == page) ? PageButtonState.ACTIVE : PageButtonState.INACTIVE;
            PageButtonDto button = PageButtonDto.builder()
                    .uri(getUri(uri, i))
                    .text(Integer.toString(i))
                    .state(state)
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    private static PageButtonDto createNextButton(int page, int totalPages, String uri) {
        if (page < totalPages) {
            int nextPage = page + 1;
            return PageButtonDto.builder()
                    .uri(getUri(uri, nextPage))
                    .text("Next")
                    .state(PageButtonState.INACTIVE)
                    .build();
        }
        return PageButtonDto.builder()
                .text("Next")
                .state(PageButtonState.DISABLED)
                .build();
    }

    private static String getUri(String uri, int page) {
        return UriComponentsBuilder.fromUriString(uri)
                .replaceQueryParam("page", page)
                .build(false)
                .toUriString();
    }

    private static String getUriWithQuery(HttpServletRequest request) {
        if (request.getQueryString() != null) {
            return request.getRequestURI() + "?" + request.getQueryString();
        } else {
            return request.getRequestURI();
        }
    }
}
