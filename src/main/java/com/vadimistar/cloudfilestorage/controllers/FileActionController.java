package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.config.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
import com.vadimistar.cloudfilestorage.utils.StringUtils;
import com.vadimistar.cloudfilestorage.utils.URLUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

        String decodedPath = URLUtils.decode(path);

        Optional<FileDto> file = fileService.statObject(user.getId(), decodedPath);

        if (file.isEmpty()) {
            return "redirect:/error?notFound";
        }

        model.addAttribute("isDirectory", file.get().isDirectory());
        model.addAttribute("name", file.get().getName());

        return "file-action";
    }

    @SneakyThrows
    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam String path,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User with this username does not exist"));

        String decodedPath = URLUtils.decode(path);

        Optional<FileDto> file = fileService.statObject(user.getId(), decodedPath);

        if (file.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/error?notFound")
                    .body(null);
        }

        if (file.get().isDirectory()) {
            decodedPath = StringUtils.addSuffix(decodedPath, "/");

            ByteArrayResource folder = new ByteArrayResource(
                    fileService.downloadDirectory(user.getId(), decodedPath)
            );

            String folderName = StringUtils.removeSuffix(decodedPath, "/");

            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + folderName + ".zip\"")
                    .body(folder);
        }

        ByteArrayResource byteArrayResource = new ByteArrayResource(
                fileService.downloadFile(user.getId(), decodedPath)
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.get().getName() + "\"")
                .body(byteArrayResource);
    }

    @SneakyThrows
    @PostMapping("/rename")
    public String rename(@RequestParam String path,
                         @RequestParam String name,
                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User with this username does not exist"));

        String decodedPath = URLUtils.decode(path);

        Optional<FileDto> file = fileService.statObject(user.getId(), decodedPath);

        if (file.isEmpty()) {
            return "redirect:/error?notFound";
        }

        String newPath;

        if (file.get().isDirectory()) {
            newPath = fileService.renameDirectory(user.getId(), decodedPath, name);
        } else {
            newPath = fileService.renameFile(user.getId(), decodedPath, name);
        }

        return "redirect:/file-action?path=" + URLUtils.encode(newPath);
    }

    @SneakyThrows
    @GetMapping("/delete")
    public String delete(@RequestParam String path,
                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User with this username is not found"));

        String decodedPath = URLUtils.decode(path);

        Optional<FileDto> file = fileService.statObject(user.getId(), decodedPath);

        if (file.isEmpty()) {
            return "redirect:/error?notFound";
        }

        if (file.get().isDirectory()) {
            fileService.deleteDirectory(user.getId(), decodedPath);
        } else {
            fileService.deleteFile(user.getId(), decodedPath);
        }

        String parentDirectory = PathUtils.getParentDirectory(decodedPath);

        return "redirect:/?path=" + URLUtils.encode(parentDirectory);
    }
}
