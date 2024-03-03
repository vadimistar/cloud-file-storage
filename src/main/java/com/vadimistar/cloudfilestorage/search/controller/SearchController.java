package com.vadimistar.cloudfilestorage.search.controller;

import com.vadimistar.cloudfilestorage.common.AuthorizedUser;
import com.vadimistar.cloudfilestorage.search.dto.FoundFileDto;
import com.vadimistar.cloudfilestorage.auth.entity.User;
import com.vadimistar.cloudfilestorage.search.service.SearchService;
import com.vadimistar.cloudfilestorage.search.mapper.FoundFileDtoMapper;
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

    @GetMapping("/search")
    public String search(@RequestParam String query,
                         @AuthorizedUser User user,
                         Model model) {
        List<FoundFileDto> foundFiles = searchService.searchFiles(user.getId(), query)
                .map(FoundFileDtoMapper::makeFoundFileDto)
                .toList();
        model.addAttribute("files", foundFiles);

        return "search";
    }
}
