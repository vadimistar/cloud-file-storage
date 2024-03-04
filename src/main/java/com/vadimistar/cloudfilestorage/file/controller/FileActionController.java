package com.vadimistar.cloudfilestorage.file.controller;

import com.vadimistar.cloudfilestorage.common.AuthorizedUser;
import com.vadimistar.cloudfilestorage.auth.entity.User;
import com.vadimistar.cloudfilestorage.common.exceptions.FileActionException;
import com.vadimistar.cloudfilestorage.file.exception.FileNotFoundException;
import com.vadimistar.cloudfilestorage.common.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.file.dto.DeleteFileRequestDto;
import com.vadimistar.cloudfilestorage.file.dto.DownloadFileRequestDto;
import com.vadimistar.cloudfilestorage.file.dto.FileActionRequestDto;
import com.vadimistar.cloudfilestorage.file.dto.RenameFileRequestDto;
import com.vadimistar.cloudfilestorage.file.service.FileService;
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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> download(@ModelAttribute @Valid DownloadFileRequestDto request,
                                      BindingResult bindingResult,
                                      @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
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
    public String rename(@ModelAttribute @Valid RenameFileRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        String newPath = fileService.renameFile(user.getId(), path, request.getName());
        return "redirect:/file-action?path=" + URLUtils.encode(newPath);
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute @Valid DeleteFileRequestDto request,
                         BindingResult bindingResult,
                         @AuthorizedUser User user) throws FileServiceException {
        if (bindingResult.hasErrors()) {
            throw new FileActionException(
                    ValidationUtils.getMessage(bindingResult), request.getPath()
            );
        }

        String path = URLUtils.decode(request.getPath());
        fileService.deleteFile(user.getId(), path);

        String parentDirectory = PathUtils.getParentDirectory(path);
        return "redirect:/?path=" + URLUtils.encode(parentDirectory);
    }
}
