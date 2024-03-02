package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.AuthorizedUser;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.dto.FoundFileDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.services.SearchService;
import com.vadimistar.cloudfilestorage.utils.FoundFileDtoMapper;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
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
