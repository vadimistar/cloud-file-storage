package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.config.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.repositories.UserRepository;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
@AllArgsConstructor
public class FileActionController {

    private final UserService userService;

    private final FileService fileService;

    @SneakyThrows
    @GetMapping("/file-action")
    public String fileActionView(@RequestParam String path,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User with this username does not exist"));

        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

        if (!fileService.isFileExists(user.getId(), decodedPath)) {
            return "redirect:/?fileNotExists";
        }

        return "file-action";
    }
}
