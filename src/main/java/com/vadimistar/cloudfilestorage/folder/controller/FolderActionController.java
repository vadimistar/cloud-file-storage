package com.vadimistar.cloudfilestorage.folder.controller;

import com.vadimistar.cloudfilestorage.common.AuthorizedUser;
import com.vadimistar.cloudfilestorage.auth.entity.User;
import com.vadimistar.cloudfilestorage.common.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.common.exceptions.FolderActionException;
import com.vadimistar.cloudfilestorage.common.exceptions.FolderNotFoundException;
import com.vadimistar.cloudfilestorage.folder.dto.DeleteFolderRequestDto;
import com.vadimistar.cloudfilestorage.folder.dto.DownloadFolderRequestDto;
import com.vadimistar.cloudfilestorage.folder.dto.FolderActionRequestDto;
import com.vadimistar.cloudfilestorage.folder.dto.RenameFolderRequestDto;
import com.vadimistar.cloudfilestorage.folder.service.FolderService;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import com.vadimistar.cloudfilestorage.common.util.ValidationUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/folder-action")
public class FolderActionController {

    private final FolderService folderService;

    @GetMapping
    public String folderAction(@ModelAttribute @Valid FolderActionRequestDto request,
                             BindingResult bindingResult,
                             @AuthorizedUser User user,
                             Model model) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        if (!folderService.isFolderExists(user.getId(), path)) {
            throw new FolderNotFoundException();
        }

        model.addAttribute("name", PathUtils.getFilename(path));

        return "folder-action";
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@ModelAttribute @Valid DownloadFolderRequestDto request,
                                      BindingResult bindingResult,
                                      @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        if (!folderService.isFolderExists(user.getId(), path)) {
            throw new FolderNotFoundException();
        }

        ByteArrayResource result = new ByteArrayResource(
                folderService.downloadFolder(user.getId(), path)
        );
        String currentDirectoryName = PathUtils.getCurrentDirectoryName(path);
        String filename = URLUtils.encode(currentDirectoryName);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".zip\"")
                .body(result);
    }

    @PostMapping("/rename")
    public String rename(@ModelAttribute @Valid RenameFolderRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        if (!folderService.isFolderExists(user.getId(), path)) {
            throw new FolderNotFoundException();
        }

        if (PathUtils.getFilename(path).equals(request.getName())) {
            return "redirect:/folder-action?path=" + URLUtils.encode(request.getPath());
        }

        String newPath = folderService.renameFolder(user.getId(), path, request.getName());
        return "redirect:/folder-action?path=" + URLUtils.encode(newPath);
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute @Valid DeleteFolderRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new FolderActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        if (!folderService.isFolderExists(user.getId(), path)) {
            throw new FolderNotFoundException();
        }

        folderService.deleteFolder(user.getId(), path);

        String parentDirectory = PathUtils.getParentDirectory(path);
        return "redirect:/?path=" + URLUtils.encode(parentDirectory);
    }
}
