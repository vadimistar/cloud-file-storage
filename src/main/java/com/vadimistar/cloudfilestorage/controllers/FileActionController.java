package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.AuthorizedUser;
import com.vadimistar.cloudfilestorage.dto.*;
import com.vadimistar.cloudfilestorage.entities.User;
import com.vadimistar.cloudfilestorage.exceptions.*;
import com.vadimistar.cloudfilestorage.services.FileService;
import com.vadimistar.cloudfilestorage.utils.PathUtils;
import com.vadimistar.cloudfilestorage.utils.URLUtils;
import com.vadimistar.cloudfilestorage.utils.ValidationUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.RoleInfoNotFoundException;

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
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        assert (path.equals(request.getPath()));
        if (!fileService.isFileExists(user.getId(), path)) {
            throw new FileNotFoundException();
        }

        model.addAttribute("name", PathUtils.getFilename(path));

        return "file-action";
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@ModelAttribute @Valid DownloadRequestDto request,
                                      BindingResult bindingResult,
                                      @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        if (!fileService.isFileExists(user.getId(), path)) {
            throw new FileNotFoundException();
        }

        ByteArrayResource byteArrayResource = new ByteArrayResource(
                fileService.downloadFile(user.getId(), path)
        );
        String filename = URLUtils.encode(PathUtils.getFilename(path));
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(byteArrayResource);
    }

    @PostMapping("/rename")
    public String rename(@ModelAttribute @Valid RenameRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        if (!fileService.isFileExists(user.getId(), path)) {
            throw new FileNotFoundException();
        }

        if (PathUtils.getFilename(path).equals(request.getName())) {
            return "redirect:/file-action?path=" + URLUtils.encode(request.getPath());
        }

        String newPath = fileService.renameFile(user.getId(), path, request.getName());
        return "redirect:/file-action?path=" + URLUtils.encode(newPath);
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute @Valid DeleteRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        if (!fileService.isFileExists(user.getId(), path)) {
            throw new FileNotFoundException();
        }

        fileService.deleteFile(user.getId(), path);

        String parentDirectory = PathUtils.getParentDirectory(path);
        return "redirect:/?path=" + URLUtils.encode(parentDirectory);
    }
}
