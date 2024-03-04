package com.vadimistar.cloudfilestorage.search.controller;

import com.vadimistar.cloudfilestorage.ApplicationConfig;
import com.vadimistar.cloudfilestorage.common.AuthorizedUser;
import com.vadimistar.cloudfilestorage.common.dto.PaginationItemDto;
import com.vadimistar.cloudfilestorage.common.util.PageUtils;
import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import com.vadimistar.cloudfilestorage.auth.entity.User;
import com.vadimistar.cloudfilestorage.search.service.SearchService;
import com.vadimistar.cloudfilestorage.search.mapper.FoundFileDtoMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Stream;

@Controller
@AllArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final ApplicationConfig applicationConfig;

    @GetMapping("/search")
    public String search(@RequestParam String query,
                         @RequestParam(required = false, defaultValue = "1") int page,
                         @AuthorizedUser User user,
                         Model model,
                         HttpServletRequest httpServletRequest) {
        List<FoundFileDto> foundFiles = searchService.searchFiles(user.getId(), query).toList();

        List<FoundFileDto> modelFiles = PageUtils
                .getPage(foundFiles.stream(), page, applicationConfig.getSearchPageSize())
                .toList();
        model.addAttribute("files", modelFiles);

        int totalPages = PageUtils.countPages(
                applicationConfig.getSearchPageSize(),
                foundFiles.size()
        );
        List<PaginationItemDto> pagination = PageUtils.createPagination(
                page, totalPages, httpServletRequest.getRequestURI() + "?" + httpServletRequest.getQueryString()
        );
        model.addAttribute("pagination", pagination);

        return "search";
    }
}
