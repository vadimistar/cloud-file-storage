package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.config.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.dto.FolderDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import com.vadimistar.cloudfilestorage.utils.NavigationFoldersParser;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
import com.vadimistar.cloudfilestorage.utils.StringUtils;
import com.vadimistar.cloudfilestorage.utils.URLUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
public class IndexController {

    private final UserService userService;

    private final FileService fileService;

    @SneakyThrows
    @GetMapping("/")
    public String indexPage(@RequestParam(required = false, defaultValue = "") String path,
                            Model model,
                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User with this username is not found"));

        String decodedPath = URLUtils.decode(path);

        if (!PathUtils.isHomeDirectory(decodedPath) && !fileService.isDirectoryExists(user.getId(), decodedPath)) {
            return "redirect:/error?notFound";
        }

        List<FolderDto> navigationFolders = NavigationFoldersParser.parseNavigationFolders(path);

        List<FolderDto> previousFolders = null;
        String currentFolder = "/";
        if (!navigationFolders.isEmpty()) {
            previousFolders = navigationFolders.subList(0, navigationFolders.size() - 1);
            currentFolder = navigationFolders.get(navigationFolders.size() - 1).getName();
        } else {
            previousFolders = new ArrayList<>();
        }

        model.addAttribute("previousFolders", previousFolders);
        model.addAttribute("currentFolder", currentFolder);

        List<FileDto> files;
        try {
            files = fileService.getFilesInDirectory(user.getId(), path);
            for (FileDto file : files) {
                file.setPath(URLUtils.encode(file.getPath()));
            }
        } catch (FileServiceException e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("files", files);

        return "index";
    }

    @SneakyThrows
    @PostMapping("/create-folder")
    public String createFolder(@RequestParam String path,
                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User with this username does not exist"));

        String decodedPath = URLUtils.decode(path);

        for (int attempts = 1; attempts <= MAX_NEW_FOLDER_ATTEMPTS; attempts ++) {
            String newFolderPath = PathUtils.getChildPath(decodedPath, "New Folder (%d)".formatted(attempts));

            if (!fileService.isDirectoryExists(user.getId(), newFolderPath)) {
                fileService.createFolder(user.getId(), newFolderPath);
                break;
            }
        }

        return "redirect:/?path=" + path;
    }

    private static final int MAX_NEW_FOLDER_ATTEMPTS = 256;
}