package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.config.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.dto.FolderDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.yaml.snakeyaml.util.UriEncoder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class IndexController {

    private final UserService userService;

    private final FileService fileService;

    @GetMapping("/")
    public String indexPage(@RequestParam(required = false, defaultValue = "") String path,
                            Model model,
                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<FolderDto> previousFolders = new ArrayList<>();
        FolderDto homeFolder = new FolderDto("/", "/");
        previousFolders.add(homeFolder);

        String[] pathParts = path.split("/");

        StringBuilder pathBuilder = new StringBuilder("/?path=");
        for (int i = 0; i < pathParts.length - 1; i ++) {
            pathBuilder.append(URLEncoder.encode(pathParts[i], StandardCharsets.UTF_8));
            previousFolders.add(new FolderDto(pathParts[i], pathBuilder.toString()));
            pathBuilder.append(URLEncoder.encode("/", StandardCharsets.UTF_8));
        }

        String currentFolder = "/";
        if (previousFolders.isEmpty()) {
            if (!path.isEmpty()) {
                currentFolder = path;
            }
        } else {
            currentFolder = pathParts[pathParts.length - 1];
        }

        model.addAttribute("previousFolders", previousFolders);
        model.addAttribute("currentFolder", currentFolder);

        User user = userService.getUserByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User with this username is not found"));

        List<FileDto> files;
        try {
            files = fileService.getFilesInFolder(user.getId(), path);
        } catch (FileServiceException e) {
            throw new RuntimeException(e);
        }

        model.addAttribute("files", files);

        return "index";
    }

}