package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.AuthorizedUser;
import com.vadimistar.cloudfilestorage.dto.*;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.exceptions.ResourceNotFoundException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/file-action")
public class FileActionController {

    private final FileService fileService;

    @GetMapping
    public String fileAction(@ModelAttribute @Valid FileActionRequestDto request,
                             @AuthorizedUser User user,
                             Model model) throws FileServiceException {
        FileDto file = fileService.statObject(user.getId(), request.getPath())
                .orElseThrow(ResourceNotFoundException::new);
        model.addAttribute("isDirectory", file.isDirectory());
        model.addAttribute("name", file.getName());
        return "file-action";
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@ModelAttribute @Valid DownloadRequestDto request,
                                      @AuthorizedUser User user) throws FileServiceException {
        FileDto file = fileService.statObject(user.getId(), request.getPath())
                .orElseThrow(ResourceNotFoundException::new);
        if (file.isDirectory()) {
            return downloadFolder(user.getId(), request.getPath());
        } else {
            return downloadFile(user.getId(), request.getPath(), file.getName());
        }
    }

    @PostMapping("/rename")
    public String rename(@ModelAttribute @Valid RenameRequestDto request,
                         @AuthorizedUser User user) throws FileServiceException {
        FileDto file = fileService.statObject(user.getId(), request.getPath())
                .orElseThrow(ResourceNotFoundException::new);
        String newPath;
        if (file.isDirectory()) {
            newPath = fileService.renameDirectory(user.getId(), request.getPath(), request.getName());
        } else {
            newPath = fileService.renameFile(user.getId(), request.getPath(), request.getName());
        }
        return "redirect:/file-action?path=" + newPath;
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute @Valid DeleteRequestDto request,
                         @AuthorizedUser User user) throws FileServiceException {
        FileDto file = fileService.statObject(user.getId(), request.getPath())
                .orElseThrow(ResourceNotFoundException::new);
        if (file.isDirectory()) {
            fileService.deleteDirectory(user.getId(), request.getPath());
        } else {
            fileService.deleteFile(user.getId(), request.getPath());
        }
        String parentDirectory = PathUtils.getParentDirectory(request.getPath());
        return "redirect:/?path=" + parentDirectory;
    }

    private ResponseEntity<?> downloadFolder(Long userId, String path) throws FileServiceException {
        ByteArrayResource result = new ByteArrayResource(
                fileService.downloadDirectory(userId, path)
        );
        String name = PathUtils.getCurrentDirectoryName(path);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + ".zip\"")
                .body(result);
    }

    private ResponseEntity<?> downloadFile(Long userId, String path, String filename) throws FileServiceException {
        ByteArrayResource byteArrayResource = new ByteArrayResource(
                fileService.downloadFile(userId, path)
        );
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(byteArrayResource);
    }
}
