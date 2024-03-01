package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.AuthorizedUser;
import com.vadimistar.cloudfilestorage.dto.*;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.*;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.services.UserService;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
import com.vadimistar.cloudfilestorage.utils.StringUtils;
import com.vadimistar.cloudfilestorage.utils.URLUtils;
import com.vadimistar.cloudfilestorage.utils.ValidationUtils;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.Banner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLDecoder;
import java.util.Optional;

@Controller
@AllArgsConstructor
@RequestMapping("/file-action")
public class FileActionController {

    private final FileService fileService;

    @GetMapping
    public String fileAction(@ModelAttribute @Valid FileActionRequestDto request,
                             BindingResult bindingResult,
                             @AuthorizedUser User user,
                             Model model) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new InvalidFileActionRequestException(ValidationUtils.getMessage(bindingResult), request.getPath());
        }

        FileDto file = fileService.statObject(user.getId(), URLUtils.decode(request.getPath()))
                .orElseThrow(ResourceNotFoundException::new);
        model.addAttribute("isDirectory", file.isDirectory());
        model.addAttribute("name", file.getName());

        return "file-action";
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@ModelAttribute @Valid DownloadRequestDto request,
                                      BindingResult bindingResult,
                                      @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new InvalidDownloadRequestException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        FileDto file = fileService.statObject(user.getId(), path)
                .orElseThrow(ResourceNotFoundException::new);

        if (file.isDirectory()) {
            return downloadFolder(user.getId(), path);
        } else {
            return downloadFile(user.getId(), path, file.getName());
        }
    }

    @PostMapping("/rename")
    public String rename(@ModelAttribute @Valid RenameRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new InvalidRenameRequestException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        FileDto file = fileService.statObject(user.getId(), path)
                .orElseThrow(ResourceNotFoundException::new);

        if (file.getName().equals(request.getName())) {
            return "redirect:/file-action?path=" + URLUtils.encode(request.getPath());
        }

        String newPath;
        if (file.isDirectory()) {
            newPath = fileService.renameDirectory(user.getId(), path, request.getName());
        } else {
            newPath = fileService.renameFile(user.getId(), path, request.getName());
        }

        return "redirect:/file-action?path=" + URLUtils.encode(newPath);
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute @Valid DeleteRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new InvalidDeleteRequestException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        FileDto file = fileService.statObject(user.getId(), path)
                .orElseThrow(ResourceNotFoundException::new);

        if (file.isDirectory()) {
            fileService.deleteDirectory(user.getId(), path);
        } else {
            fileService.deleteFile(user.getId(), path);
        }

        String parentDirectory = PathUtils.getParentDirectory(path);
        return "redirect:/?path=" + URLUtils.encode(parentDirectory);
    }

    private ResponseEntity<?> downloadFolder(Long userId, String path) throws FileServiceException {
        ByteArrayResource result = new ByteArrayResource(
                fileService.downloadDirectory(userId, path)
        );
        String currentDirectoryName = PathUtils.getCurrentDirectoryName(path);
        String filename = URLUtils.encode(currentDirectoryName);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".zip\"")
                .body(result);
    }

    private ResponseEntity<?> downloadFile(Long userId, String path, String name) throws FileServiceException {
        ByteArrayResource byteArrayResource = new ByteArrayResource(
                fileService.downloadFile(userId, path)
        );
        String filename = URLUtils.encode(name);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(byteArrayResource);
    }
}
