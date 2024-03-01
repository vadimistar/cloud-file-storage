package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.AuthorizedUser;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.dto.BreadcrumbElementDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import com.vadimistar.cloudfilestorage.utils.BreadcrumbParser;
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
import java.net.URL;
import java.util.List;

@Controller
@AllArgsConstructor
public class IndexController {

    private final UserService userService;
    private final FileService fileService;

    @GetMapping("/")
    public String indexPage(@RequestParam(required = false, defaultValue = "") String path,
                            Model model,
                            @AuthorizedUser User user) throws FileServiceException {
        path = URLUtils.decode(path);
        if (!fileService.isDirectoryExists(user.getId(), path)) {
            if (PathUtils.isHomeDirectory(path)) {
                fileService.createNamedFolder(user.getId(), path);
            } else {
                throw new ResourceNotFoundException();
            }
        }

        List<BreadcrumbElementDto> breadcrumb = BreadcrumbParser.parseBreadcrumb(path);
        model.addAttribute("breadcrumb", breadcrumb);

        List<FileDto> files = fileService.getFilesInDirectory(user.getId(), path);
        for (FileDto file : files) {
            file.setPath(URLUtils.encode(file.getPath()));
        }
        model.addAttribute("files", files);

        return "index";
    }

    @PostMapping("/create-folder")
    public String createFolder(@RequestParam String path,
                               @AuthorizedUser User user) throws FileServiceException {
        fileService.createUnnamedFolder(user.getId(), URLUtils.decode(path), MAX_NEW_FOLDER_ATTEMPTS);
        return "redirect:/?path=" + URLUtils.encode(path);
    }

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile[] files,
                         @RequestParam String path,
                         @AuthorizedUser User user) throws FileServiceException, IOException {
        fileService.uploadFolder(user.getId(), files, URLUtils.decode(path));
        return "redirect:/?path=" + URLUtils.encode(path);
    }

    private static final int MAX_NEW_FOLDER_ATTEMPTS = 256;
}