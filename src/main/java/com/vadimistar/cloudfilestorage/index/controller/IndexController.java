package com.vadimistar.cloudfilestorage.index.controller;

import com.vadimistar.cloudfilestorage.common.config.ApplicationConfig;
import com.vadimistar.cloudfilestorage.common.util.AuthorizedUser;
import com.vadimistar.cloudfilestorage.common.dto.FileDto;
import com.vadimistar.cloudfilestorage.index.breadcrumbs.service.BreadcrumbsService;
import com.vadimistar.cloudfilestorage.page.dto.PaginationItemDto;
import com.vadimistar.cloudfilestorage.page.service.PageService;
import com.vadimistar.cloudfilestorage.index.breadcrumbs.dto.BreadcrumbsElementDto;
import com.vadimistar.cloudfilestorage.auth.entity.User;
import com.vadimistar.cloudfilestorage.common.exception.FolderNotFoundException;
import com.vadimistar.cloudfilestorage.folder.service.FolderService;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@AllArgsConstructor
public class IndexController {

    private final FolderService folderService;
    private final ApplicationConfig applicationConfig;
    private final PageService pageService;
    private final BreadcrumbsService breadcrumbsService;

    @GetMapping("/")
    public String indexPage(@RequestParam(required = false, defaultValue = "") String path,
                            @RequestParam(required = false, defaultValue = "1") int page,
                            Model model,
                            @AuthorizedUser User user,
                            HttpServletRequest httpServletRequest) {
        path = URLUtils.decode(path);
        if (!folderService.isFolderExists(user.getId(), path)) {
            if (PathUtils.isHomeDirectory(path)) {
                folderService.createFolder(user.getId(), path);
            } else {
                throw new FolderNotFoundException();
            }
        }

        List<BreadcrumbsElementDto> breadcrumbs = breadcrumbsService.createBreadcrumbs(path);
        model.addAttribute("breadcrumbs", breadcrumbs);

        List<FileDto> folderContent = folderService.getFolderContent(user.getId(), path).toList();

        List<FileDto> entries = pageService
                .getPage(folderContent.stream(), page, applicationConfig.getIndexPageSize())
                .peek(entry -> entry.setPath(URLUtils.encode(entry.getPath())))
                .toList();
        model.addAttribute("entries", entries);

        int totalPages = pageService.countPages(
                applicationConfig.getIndexPageSize(),
                folderContent.size()
        );
        List<PaginationItemDto> pagination = pageService.createPagination(
                page, totalPages, httpServletRequest.getRequestURI() + "?" + httpServletRequest.getQueryString()
        );
        model.addAttribute("pagination", pagination);

        return "index";
    }

    @PostMapping("/create-folder")
    public String createFolder(@RequestParam String path,
                               @AuthorizedUser User user) {
        folderService.createUnnamedFolder(user.getId(), URLUtils.decode(path), MAX_NEW_FOLDER_ATTEMPTS);
        return "redirect:/?path=" + URLUtils.encode(path);
    }

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile[] files,
                         @RequestParam String path,
                         @AuthorizedUser User user) {
        folderService.uploadFolder(user.getId(), files, URLUtils.decode(path));
        return "redirect:/?path=" + URLUtils.encode(path);
    }

    private static final int MAX_NEW_FOLDER_ATTEMPTS = 256;
}