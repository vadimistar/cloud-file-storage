package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.AuthorizedUser;
import com.vadimistar.cloudfilestorage.dto.FileDto;
import com.vadimistar.cloudfilestorage.dto.RenameRequestDto;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.vadimistar.cloudfilestorage.exceptions.UserNotLoggedInException;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
import com.vadimistar.cloudfilestorage.utils.StringUtils;
import com.vadimistar.cloudfilestorage.utils.URLUtils;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class FileActionController {

    private final UserService userService;

    private final FileService fileService;

    @SneakyThrows
    @GetMapping("/file-action")
    public String fileAction(@RequestParam String path,
                             @AuthorizedUser User user,
                             Model model) {
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
                                      @AuthorizedUser User user) {
        String decodedPath = URLUtils.decode(path);

        Optional<FileDto> file = fileService.statObject(user.getId(), decodedPath);

        if (file.isEmpty()) {
            throw new ResourceNotFoundException();
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
    public String rename(@ModelAttribute @Valid RenameRequestDto request,
                         @AuthorizedUser User user) {
        String decodedPath = URLUtils.decode(request.getPath());

        Optional<FileDto> file = fileService.statObject(user.getId(), decodedPath);

        if (file.isEmpty()) {
            return "redirect:/error?notFound";
        }

        String newPath;

        if (file.get().isDirectory()) {
            newPath = fileService.renameDirectory(user.getId(), decodedPath, request.getName());
        } else {
            newPath = fileService.renameFile(user.getId(), decodedPath, request.getName());
        }

        return "redirect:/file-action?path=" + URLUtils.encode(newPath);
    }

    @SneakyThrows
    @GetMapping("/delete")
    public String delete(@RequestParam String path,
                         @AuthorizedUser User user) {
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
