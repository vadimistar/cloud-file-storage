package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.AuthorizedUser;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.dto.BreadcrumbsElementDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.exceptions.FolderNotFoundException;
import com.vadimistar.cloudfilestorage.services.FolderService;
import com.vadimistar.cloudfilestorage.utils.BreadcrumbsCreator;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
import com.vadimistar.cloudfilestorage.utils.URLUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@AllArgsConstructor
public class IndexController {

    private final FolderService folderService;

    @GetMapping("/")
    public String indexPage(@RequestParam(required = false, defaultValue = "") String path,
                            Model model,
                            @AuthorizedUser User user) throws FileServiceException {
        path = URLUtils.decode(path);
        if (!folderService.isFolderExists(user.getId(), path)) {
            if (PathUtils.isHomeDirectory(path)) {
                folderService.createFolder(user.getId(), path);
            } else {
                throw new FolderNotFoundException();
            }
        }

        List<BreadcrumbsElementDto> breadcrumbs = BreadcrumbsCreator.createBreadcrumbs(path);
        model.addAttribute("breadcrumbs", breadcrumbs);

        List<FileDto> entries = folderService.getFolderContent(user.getId(), path)
                .peek(entry -> entry.setPath(URLUtils.encode(entry.getPath())))
                .toList();
        model.addAttribute("entries", entries);

        return "index";
    }

    @PostMapping("/create-folder")
    public String createFolder(@RequestParam String path,
                               @AuthorizedUser User user) throws FileServiceException {
        folderService.createUnnamedFolder(user.getId(), URLUtils.decode(path), MAX_NEW_FOLDER_ATTEMPTS);
        return "redirect:/?path=" + URLUtils.encode(path);
    }

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile[] files,
                         @RequestParam String path,
                         @AuthorizedUser User user) throws FileServiceException, IOException {
        folderService.uploadFolder(user.getId(), files, URLUtils.decode(path));
        return "redirect:/?path=" + URLUtils.encode(path);
    }

    private static final int MAX_NEW_FOLDER_ATTEMPTS = 256;
}