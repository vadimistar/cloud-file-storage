package com.vadimistar.cloudfilestorage.index.controller;

import com.vadimistar.cloudfilestorage.index.breadcrumbs.BreadcrumbsUtils;
import com.vadimistar.cloudfilestorage.index.config.IndexConfig;
import com.vadimistar.cloudfilestorage.security.details.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.index.breadcrumbs.BreadcrumbsElementDto;
import com.vadimistar.cloudfilestorage.folder.service.FolderService;
import com.vadimistar.cloudfilestorage.common.utils.path.PathUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class IndexController {

    private final FolderService folderService;
    private final IndexConfig indexConfig;

    @GetMapping("/")
    public String indexPage(@RequestParam(required = false, defaultValue = "") String path,
                            @RequestParam(required = false, defaultValue = "1") int page,
                            Model model,
                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (PathUtils.isHomeDirectory(path)) {
            createHomeDirectoryIfNotExists(userDetails.getUserId());
        }

        List<BreadcrumbsElementDto> breadcrumbs = BreadcrumbsUtils.createBreadcrumbs(path);
        model.addAttribute("breadcrumbs", breadcrumbs);

        Page<FileDto> filesPage = folderService.getFolderContent(
                userDetails.getUserId(),
                path,
                PageRequest.of(page - 1, indexConfig.getPageSize()));
        model.addAttribute("filesPage", filesPage);

        return "index";
    }

    private void createHomeDirectoryIfNotExists(long userId) {
        if (!folderService.isFolderExists(userId, HOME_DIRECTORY)) {
            folderService.createFolder(userId, HOME_DIRECTORY);
        }
    }

    private static final String HOME_DIRECTORY = "/";
}