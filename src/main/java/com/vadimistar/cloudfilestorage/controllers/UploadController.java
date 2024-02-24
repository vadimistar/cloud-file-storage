package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.config.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import com.vadimistar.cloudfilestorage.utils.URLUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
@AllArgsConstructor
public class UploadController {

    private final FileService fileService;

    private final UserService userService;

    @PostMapping("/upload")
    public String upload(@RequestParam(value = "file") MultipartFile[] files,
                           @RequestParam(value = "path") String path,
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (files.length != 0) {
            User user = userService.getUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User with this username is not found"));

            String directory = URLUtils.decode(path);

            for (MultipartFile file : files) {
                try {
                    fileService.uploadFile(user.getId(), file.getInputStream(), file.getSize(), directory + file.getOriginalFilename());
                } catch (IOException | FileServiceException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return "redirect:/?path=" + path;
    }
}
