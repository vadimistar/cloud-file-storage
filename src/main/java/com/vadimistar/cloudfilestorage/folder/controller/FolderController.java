package com.vadimistar.cloudfilestorage.folder.controller;

import com.vadimistar.cloudfilestorage.folder.exception.FolderActionException;
import com.vadimistar.cloudfilestorage.folder.exception.FolderNotFoundException;
import com.vadimistar.cloudfilestorage.folder.service.FolderService;
import com.vadimistar.cloudfilestorage.folder.exception.UploadFolderException;
import com.vadimistar.cloudfilestorage.folder.dto.*;
import com.vadimistar.cloudfilestorage.security.details.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.common.util.path.PathUtils;
import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import com.vadimistar.cloudfilestorage.common.util.ValidationUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/folder")
public class FolderController {

    private final FolderService folderService;

    @GetMapping("/action")
    public String action(@ModelAttribute @Valid FolderActionRequestDto request,
                       BindingResult bindingResult,
                       @AuthenticationPrincipal UserDetailsImpl userDetails,
                       Model model) {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        if (!folderService.isFolderExists(userDetails.getUserId(), request.getPath())) {
            throw new FolderNotFoundException("Folder is not found: " + request.getPath());
        }
        model.addAttribute("name", PathUtils.getFilename(request.getPath()));
        return "folder-action";
    }

    @PostMapping("/create")
    public String create(@RequestParam(required = false, defaultValue = "") String path,
                         @AuthenticationPrincipal UserDetailsImpl userDetails,
                         RedirectAttributes redirectAttributes) {
        folderService.createUnnamedFolder(userDetails.getUserId(), path, MAX_NEW_FOLDER_ATTEMPTS);
        redirectAttributes.addAttribute("path", path);
        return "redirect:/";
    }

    @PostMapping("/upload")
    public String upload(@ModelAttribute @Valid UploadFolderRequestDto request,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetailsImpl userDetails,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            throw new UploadFolderException(
                    ValidationUtils.getMessage(bindingResult),
                    request.getPath()
            );
        }
        folderService.uploadFolder(userDetails.getUserId(), request.getFiles(), request.getPath());
        redirectAttributes.addAttribute("path", request.getPath());
        return "redirect:/";
    }

    @GetMapping
    public ResponseEntity<?> download(@ModelAttribute @Valid DownloadFolderRequestDto request,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        ByteArrayResource result = new ByteArrayResource(
                folderService.downloadFolder(userDetails.getUserId(), request.getPath())
        );
        String currentDirectoryName = PathUtils.getCurrentDirectoryName(request.getPath());
        String filename = URLUtils.encode(currentDirectoryName);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".zip\"")
                .body(result);
    }

    @PatchMapping
    public String rename(@ModelAttribute @Valid RenameFolderRequestDto request,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetailsImpl userDetails,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        String newPath = folderService.renameFolder(userDetails.getUserId(), request.getPath(), request.getName());
        redirectAttributes.addAttribute("path", newPath);
        return "redirect:/folder/action";
    }

    @DeleteMapping
    public String delete(@ModelAttribute @Valid DeleteFolderRequestDto request,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetailsImpl userDetails,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        folderService.deleteFolder(userDetails.getUserId(), request.getPath());
        String parentDirectory = PathUtils.getParentDirectory(request.getPath());
        redirectAttributes.addAttribute("path", parentDirectory);
        return "redirect:/";
    }

    private static final int MAX_NEW_FOLDER_ATTEMPTS = 256;
}
