package com.vadimistar.cloudfilestorage.index.controller;

import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import com.vadimistar.cloudfilestorage.config.ApplicationConfig;
import com.vadimistar.cloudfilestorage.argument_resolver.AuthorizedUser;
import com.vadimistar.cloudfilestorage.index.exception.InvalidIndexPageException;
import com.vadimistar.cloudfilestorage.index.breadcrumbs.BreadcrumbsUtils;
import com.vadimistar.cloudfilestorage.security.dto.UserDto;
import com.vadimistar.cloudfilestorage.common.exception.InvalidPageException;
import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.common.util.page.PageButtonDto;
import com.vadimistar.cloudfilestorage.index.breadcrumbs.BreadcrumbsElementDto;
import com.vadimistar.cloudfilestorage.folder.exception.FolderNotFoundException;
import com.vadimistar.cloudfilestorage.folder.service.FolderService;
import com.vadimistar.cloudfilestorage.common.util.page.PageUtils;
import com.vadimistar.cloudfilestorage.common.util.path.PathUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class IndexController {

    private final FolderService folderService;
    private final ApplicationConfig appConfig;

    @GetMapping("/")
    public String indexPage(@RequestParam(required = false, defaultValue = "") String path,
                            @RequestParam(required = false, defaultValue = "1") int page,
                            Model model,
                            @AuthorizedUser UserDto user,
                            HttpServletRequest request) {
        path = URLUtils.decode(path);
        if (PathUtils.isHomeDirectory(path)) {
            createHomeDirectoryIfNotExists(user.getId());
        }

        if (!folderService.isFolderExists(user.getId(), path)) {
            throw new FolderNotFoundException("Home directory does not exist");
        }

        List<BreadcrumbsElementDto> breadcrumbs = BreadcrumbsUtils.createBreadcrumbs(path);
        model.addAttribute("breadcrumbs", breadcrumbs);

        List<FileDto> folderContent = folderService.getFolderContent(user.getId(), path)
                .peek(file -> file.setPath(URLUtils.encode(file.getPath())))
                .toList();
        try {
            List<FileDto> pageFiles = PageUtils.getPage(folderContent, appConfig.getIndexPageSize(), page);
            model.addAttribute("files", pageFiles);
        } catch (InvalidPageException e) {
            throw new InvalidIndexPageException("Invalid page");
        }

        List<PageButtonDto> pageButtons = PageUtils.createPageButtons(
                folderContent.size(), appConfig.getIndexPageSize(), page, request
        );
        model.addAttribute("pageButtons", pageButtons);

        return "index";
    }

    private void createHomeDirectoryIfNotExists(long userId) {
        if (!folderService.isFolderExists(userId, HOME_DIRECTORY)) {
            folderService.createFolder(userId, HOME_DIRECTORY);
        }
    }

    private static final String HOME_DIRECTORY = "/";
}