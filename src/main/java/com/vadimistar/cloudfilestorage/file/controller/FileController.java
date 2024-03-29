package com.vadimistar.cloudfilestorage.file.controller;

import com.vadimistar.cloudfilestorage.file.service.FileService;
import com.vadimistar.cloudfilestorage.file.dto.DeleteFileRequestDto;
import com.vadimistar.cloudfilestorage.file.dto.DownloadFileRequestDto;
import com.vadimistar.cloudfilestorage.file.dto.FileActionRequestDto;
import com.vadimistar.cloudfilestorage.file.dto.RenameFileRequestDto;
import com.vadimistar.cloudfilestorage.file.exception.FileActionException;
import com.vadimistar.cloudfilestorage.file.exception.FileNotFoundException;
import com.vadimistar.cloudfilestorage.security.details.UserDetailsImpl;
import com.vadimistar.cloudfilestorage.common.utils.path.PathUtils;
import com.vadimistar.cloudfilestorage.common.utils.URLUtils;
import com.vadimistar.cloudfilestorage.common.utils.ValidationUtils;
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
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;

    @GetMapping("/action")
    public String action(@ModelAttribute @Valid FileActionRequestDto request,
                       BindingResult bindingResult,
                       @AuthenticationPrincipal UserDetailsImpl userDetails,
                       Model model) {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        if (!fileService.isFileExists(userDetails.getUserId(), request.getPath())) {
            throw new FileNotFoundException("File is not found: " + request.getPath());
        }
        model.addAttribute("name", PathUtils.getFilename(request.getPath()));
        return "file-action";
    }

    @GetMapping
    public ResponseEntity<?> download(@ModelAttribute @Valid DownloadFileRequestDto request,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        ByteArrayResource byteArrayResource = new ByteArrayResource(
                fileService.downloadFile(userDetails.getUserId(), request.getPath())
        );
        String filename = URLUtils.encode(PathUtils.getFilename(request.getPath()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(byteArrayResource);
    }

    @PatchMapping
    public String rename(@ModelAttribute @Valid RenameFileRequestDto request,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetailsImpl userDetails,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        String newPath = fileService.renameFile(userDetails.getUserId(), request.getPath(), request.getName());
        redirectAttributes.addAttribute("path", newPath);
        return "redirect:/file/action";
    }

    @DeleteMapping
    public String delete(@ModelAttribute @Valid DeleteFileRequestDto request,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetailsImpl userDetails,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }
        fileService.deleteFile(userDetails.getUserId(), request.getPath());
        String parentDirectory = PathUtils.getParentDirectory(request.getPath());
        redirectAttributes.addAttribute("path", parentDirectory);
        return "redirect:/";
    }
}
