package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.config.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import com.vadimistar.cloudfilestorage.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.vadimistar.cloudfilestorage.utils.PathUtils.getRelativePath;
import static com.vadimistar.cloudfilestorage.utils.StringUtils.addSuffix;

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

        Optional<FileDto> file = fileService.statFile(user.getId(), decodedPath);

        if (file.isEmpty()) {
            return "redirect:/?fileNotExists";
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

        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);

        Optional<FileDto> file = fileService.statFile(user.getId(), decodedPath);

        if (file.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/?fileNotExists")
                    .body(null);
        }

        if (file.get().isDirectory()) {
            decodedPath = StringUtils.addSuffix(decodedPath, "/");

            ByteArrayResource folder = new ByteArrayResource(
                    fileService.downloadFolder(user.getId(), decodedPath)
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
}
