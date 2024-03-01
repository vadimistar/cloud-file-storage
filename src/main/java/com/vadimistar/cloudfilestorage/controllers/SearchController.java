package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.AuthorizedUser;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.dto.FoundFileDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.SearchServiceException;
import com.vadimistar.cloudfilestorage.services.SearchService;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public String search(@RequestParam String query,
                         @AuthorizedUser User user,
                         Model model) throws SearchServiceException {
        List<FileDto> files = searchService.searchFiles(user.getId(), query);
        List<FoundFileDto> foundFiles = files.stream()
                        .map(file -> FoundFileDto.builder()
                                .name(file.getName())
                                .isDirectory(file.isDirectory())
                                .parentPath(PathUtils.getParentDirectory(file.getPath()))
                                .build()
                        )
                        .toList();
        model.addAttribute("files", foundFiles);

        return "search";
    }
}
