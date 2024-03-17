package com.vadimistar.cloudfilestorage.file.controller;

import com.vadimistar.cloudfilestorage.file.service.FileService;
import com.vadimistar.cloudfilestorage.file.dto.DeleteFileRequestDto;
import com.vadimistar.cloudfilestorage.file.dto.DownloadFileRequestDto;
import com.vadimistar.cloudfilestorage.file.dto.FileViewRequestDto;
import com.vadimistar.cloudfilestorage.file.dto.RenameFileRequestDto;
import com.vadimistar.cloudfilestorage.file.exception.FileActionException;
import com.vadimistar.cloudfilestorage.file.exception.FileNotFoundException;
import com.vadimistar.cloudfilestorage.security.dto.UserDto;
import com.vadimistar.cloudfilestorage.argument_resolver.AuthorizedUser;
import com.vadimistar.cloudfilestorage.common.util.path.PathUtils;
import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import com.vadimistar.cloudfilestorage.common.validation.ValidationUtils;
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
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    @GetMapping
    public String file(@ModelAttribute @Valid FileViewRequestDto request,
                       BindingResult bindingResult,
                       @AuthorizedUser UserDto user,
                       Model model) {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        String decodedPath = URLUtils.decode(request.getPath());
        if (!fileService.isFileExists(user.getId(), decodedPath)) {
            throw new FileNotFoundException("File is not found: " + decodedPath);
        }
        model.addAttribute("name", PathUtils.getFilename(decodedPath));
        return "file";
    }

    @GetMapping("/download")
    public ResponseEntity<?> download(@ModelAttribute @Valid DownloadFileRequestDto request,
                                      BindingResult bindingResult,
                                      @AuthorizedUser UserDto user) {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        String decodedPath = URLUtils.decode(request.getPath());
        ByteArrayResource byteArrayResource = new ByteArrayResource(
                fileService.downloadFile(user.getId(), decodedPath)
        );
        String filename = URLUtils.encode(PathUtils.getFilename(decodedPath));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(byteArrayResource);
    }

    @PostMapping("/rename")
    public String rename(@ModelAttribute @Valid RenameFileRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser UserDto user) {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        String decodedPath = URLUtils.decode(request.getPath());
        String newPath = fileService.renameFile(user.getId(), decodedPath, request.getName());
        return "redirect:/file?path=" + URLUtils.encode(newPath);
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute @Valid DeleteFileRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser UserDto user) {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        String decodedPath = URLUtils.decode(request.getPath());
        fileService.deleteFile(user.getId(), decodedPath);
        String parentDirectory = PathUtils.getParentDirectory(request.getPath());
        return "redirect:/?path=" + URLUtils.encode(parentDirectory);
    }
}
