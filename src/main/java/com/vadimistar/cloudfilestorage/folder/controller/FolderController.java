package com.vadimistar.cloudfilestorage.folder.controller;

import com.vadimistar.cloudfilestorage.folder.exception.FolderActionException;
import com.vadimistar.cloudfilestorage.folder.exception.FolderNotFoundException;
import com.vadimistar.cloudfilestorage.folder.service.FolderService;
import com.vadimistar.cloudfilestorage.folder.exception.UploadFolderException;
import com.vadimistar.cloudfilestorage.folder.dto.*;
import com.vadimistar.cloudfilestorage.security.dto.UserDto;
import com.vadimistar.cloudfilestorage.argument_resolver.AuthorizedUser;
import com.vadimistar.cloudfilestorage.common.util.path.PathUtils;
import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import com.vadimistar.cloudfilestorage.validation.ValidationUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
@RequestMapping("/folder")
public class FolderController {

    private final FolderService folderService;

    @GetMapping
    public String folder(@ModelAttribute @Valid FolderViewRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser UserDto user,
                         Model model) {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        String decodedPath = URLUtils.decode(request.getPath());
        if (!folderService.isFolderExists(user.getId(), decodedPath)) {
            throw new FolderNotFoundException();
        }
        model.addAttribute("name", PathUtils.getFilename(decodedPath));
        return "folder";
    }

    @PostMapping("/create")
    public String createFolder(@RequestParam(required = false, defaultValue = "") String path,
                               @AuthorizedUser UserDto user) {
        String decodedPath = URLUtils.decode(path);
        folderService.createUnnamedFolder(user.getId(), decodedPath, MAX_NEW_FOLDER_ATTEMPTS);
        return "redirect:/?path=" + path;
    }

    @PostMapping("/upload")
    public String upload(@ModelAttribute @Valid UploadFolderRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser UserDto user) {
        if (bindingResult.hasErrors()) {
            throw new UploadFolderException(
                    ValidationUtils.getMessage(bindingResult),
                    request.getPath()
            );
        }
        String decodedPath = URLUtils.decode(request.getPath());
        folderService.uploadFolder(user.getId(), request.getFiles(), decodedPath);
        return "redirect:/?path=" + request.getPath();
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@ModelAttribute @Valid DownloadFolderRequestDto request,
                                      BindingResult bindingResult,
                                      @AuthorizedUser UserDto user) {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        String decodedPath = URLUtils.decode(request.getPath());
        ByteArrayResource result = new ByteArrayResource(
                folderService.downloadFolder(user.getId(), decodedPath)
        );
        String currentDirectoryName = PathUtils.getCurrentDirectoryName(decodedPath);
        String filename = URLUtils.encode(currentDirectoryName);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".zip\"")
                .body(result);
    }

    @PostMapping("/rename")
    public String rename(@ModelAttribute @Valid RenameFolderRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser UserDto user) {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        String decodedPath = URLUtils.decode(request.getPath());
        String newPath = folderService.renameFolder(user.getId(), decodedPath, request.getName());
        return "redirect:/folder?path=" + URLUtils.encode(newPath);
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute @Valid DeleteFolderRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser UserDto user) {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        String decodedPath = URLUtils.decode(request.getPath());
        folderService.deleteFolder(user.getId(), decodedPath);
        String parentDirectory = PathUtils.getParentDirectory(decodedPath);
        return "redirect:/?path=" + URLUtils.encode(parentDirectory);
    }

    private static final int MAX_NEW_FOLDER_ATTEMPTS = 256;
}
