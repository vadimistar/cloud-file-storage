package com.vadimistar.cloudfilestorage.search.controller;

import com.vadimistar.cloudfilestorage.search.config.SearchConfig;
import com.vadimistar.cloudfilestorage.search.service.SearchService;
import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import com.vadimistar.cloudfilestorage.security.details.UserDetailsImpl;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final SearchConfig searchConfig;

    @GetMapping("/search")
    public String search(@RequestParam String query,
                         @RequestParam(required = false, defaultValue = "1") int page,
                         @AuthenticationPrincipal UserDetailsImpl userDetails,
                         Model model) {
        Page<FoundFileDto> filesPage = searchService.searchFiles(
                userDetails.getUserId(),
                query,
                PageRequest.of(page - 1, searchConfig.getPageSize()));
        model.addAttribute("filesPage", filesPage);

        return "search";
    }
}
