package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.exceptions.UserNotLoggedInException;
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
import java.security.Principal;

@Controller
@AllArgsConstructor
public class UploadController {

    private final FileService fileService;

    private final UserService userService;

    @PostMapping("/upload")
    public String upload(@RequestParam(value = "file") MultipartFile[] files,
                         @RequestParam(value = "path") String path,
                         Principal principal) {
        User user = userService.getUserByUsername(principal.getName())
                .orElseThrow(UserNotLoggedInException::new);
        if (files.length != 0) {
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
