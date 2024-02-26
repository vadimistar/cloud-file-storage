package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.AuthorizedUser;
import com.vadimistar.cloudfilestorage.dto.CreateFolderRequestDto;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.dto.BreadcrumbElementDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import com.vadimistar.cloudfilestorage.utils.BreadcrumbParser;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        if (!fileService.isDirectoryExists(user.getId(), path)) {
            throw new ResourceNotFoundException();
        }

        List<BreadcrumbElementDto> breadcrumb = BreadcrumbParser.parseBreadcrumb(path);
        model.addAttribute("breadcrumb", breadcrumb);

        List<FileDto> files = fileService.getFilesInDirectory(user.getId(), path);
        model.addAttribute("files", files);

        return "index";
    }

    @PostMapping("/create-folder")
    public String createFolder(@ModelAttribute @Valid CreateFolderRequestDto request,
                               @AuthorizedUser User user) throws FileServiceException {
        fileService.createUnnamedFolder(user.getId(), request.getPath(), MAX_NEW_FOLDER_ATTEMPTS);
        return "redirect:/?path=" + request.getPath();
    }

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile[] files,
                         @RequestParam String path,
                         @AuthorizedUser User user) throws FileServiceException, IOException {
        fileService.uploadFolder(user.getId(), files, path);
        return "redirect:/?path=" + path;
    }

    private static final int MAX_NEW_FOLDER_ATTEMPTS = 256;
}