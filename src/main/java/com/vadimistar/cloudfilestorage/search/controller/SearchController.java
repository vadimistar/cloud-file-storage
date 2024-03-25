package com.vadimistar.cloudfilestorage.search.controller;

import com.vadimistar.cloudfilestorage.config.ApplicationConfig;
import com.vadimistar.cloudfilestorage.search.service.SearchService;
import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import com.vadimistar.cloudfilestorage.search.exception.InvalidSearchPageException;
import com.vadimistar.cloudfilestorage.security.dto.UserDto;
import com.vadimistar.cloudfilestorage.common.exception.InvalidPageException;
import com.vadimistar.cloudfilestorage.common.argument_resolver.AuthorizedUser;
import com.vadimistar.cloudfilestorage.common.util.page.PageButtonDto;
import com.vadimistar.cloudfilestorage.common.util.page.PageUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final ApplicationConfig appConfig;

    @GetMapping("/search")
    public String search(@RequestParam String query,
                         @RequestParam(required = false, defaultValue = "1") int page,
                         @AuthorizedUser UserDto user,
                         Model model,
                         HttpServletRequest request) {
        List<FoundFileDto> foundFiles = searchService.searchFiles(user.getId(), query);

        try {
            List<FoundFileDto> pageFiles = PageUtils.getPage(foundFiles, appConfig.getSearchPageSize(), page);
            model.addAttribute("files", pageFiles);
        } catch (InvalidPageException e) {
            throw new InvalidSearchPageException(e.getMessage());
        }

        List<PageButtonDto> pageButtons = PageUtils.createPageButtons(
                foundFiles.size(), appConfig.getSearchPageSize(), page, request
        );
        model.addAttribute("pageButtons", pageButtons);

        return "search";
    }
}
