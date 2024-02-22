package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.config.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class FileActionController {

    private final UserService userService;

    private final FileService fileService;

    @SneakyThrows
    @GetMapping("/file-action")
    public String fileAction(@RequestParam String path,
                             @AuthenticationPrincipal UserDetailsImpl userDetails,
                             Model model) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User with this username does not exist"));

        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

        boolean fileExists = fileService.isFileExists(user.getId(), decodedPath);
        boolean folderExists = fileService.isFolderExists(user.getId(), decodedPath);

        if (!fileExists && !folderExists) {
            return "redirect:/?fileNotExists";
        }

        model.addAttribute("isDirectory", fileExists && folderExists);
        model.addAttribute("name", PathUtils.getFilename(decodedPath));

        return "file-action";
    }

    @SneakyThrows
    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam String path,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // TODO: Folder download

        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User with this username does not exist"));

        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        String filename = PathUtils.getFilename(decodedPath);

        if (!fileService.isFileExists(user.getId(), decodedPath)) {
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/?fileNotExists")
                    .body(null);
        }

        ByteArrayResource file = new ByteArrayResource(
                fileService.downloadFile(user.getId(), decodedPath)
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }
}
