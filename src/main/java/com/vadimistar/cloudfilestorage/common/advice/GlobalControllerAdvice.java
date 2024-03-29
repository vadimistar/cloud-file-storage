package com.vadimistar.cloudfilestorage.common.advice;

import com.vadimistar.cloudfilestorage.common.exception.ResourceNotFoundException;
import com.vadimistar.cloudfilestorage.file.exception.FileActionException;
import com.vadimistar.cloudfilestorage.file.exception.InvalidFilePathException;
import com.vadimistar.cloudfilestorage.folder.exception.FolderActionException;
import com.vadimistar.cloudfilestorage.folder.exception.UploadFolderException;
import com.vadimistar.cloudfilestorage.minio.exception.ResourceAlreadyExistsException;
import com.vadimistar.cloudfilestorage.security.exception.UserNotLoggedInException;
import com.vadimistar.cloudfilestorage.common.utils.path.PathUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
@Log4j2
public class GlobalControllerAdvice {

    @ExceptionHandler(UserNotLoggedInException.class)
    public String handleUserNotLoggedInException(UserNotLoggedInException e) {
        return "redirect:/login";
    }

    @ExceptionHandler(FileActionException.class)
    public RedirectView handleFileActionException(FileActionException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        redirectAttributes.addAttribute("path", e.getPath());
        return new RedirectView("/file/action", true);
    }

    @ExceptionHandler(FolderActionException.class)
    public RedirectView handleFolderActionException(FolderActionException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        redirectAttributes.addAttribute("path", e.getPath());
        return new RedirectView("/folder/action", true);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public RedirectView handleResourceNotFoundException(ResourceNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/", true);
    }

    @ExceptionHandler(UploadFolderException.class)
    public RedirectView handleUploadFileException(UploadFolderException e, RedirectAttributes redirectAttributes) {
        String path = Objects.requireNonNullElse(e.getPath(), "");
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        redirectAttributes.addAttribute("path", path);
        return new RedirectView("/", true);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public RedirectView handleResourceAlreadyExistsException(ResourceAlreadyExistsException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        String parentDirectory = PathUtils.getParentDirectory(e.getPath());
        redirectAttributes.addAttribute("path", parentDirectory);
        return new RedirectView("/", true);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public RedirectView handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", String.format("Invalid value '%s' for parameter '%s'", e.getValue(), e.getName()));
        return new RedirectView("/error", true);
    }

    @ExceptionHandler(InvalidFilePathException.class)
    public RedirectView handleInvalidFilePathException(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/", true);
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Exception: " + e.getClass() + " message: " + e.getMessage());
        String stackTrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
        log.error(stackTrace);
        return new RedirectView("/error", true);
    }
}
