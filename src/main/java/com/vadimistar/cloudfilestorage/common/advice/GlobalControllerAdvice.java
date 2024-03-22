package com.vadimistar.cloudfilestorage.common.advice;

import com.vadimistar.cloudfilestorage.common.exception.ResourceNotFoundException;
import com.vadimistar.cloudfilestorage.file.exception.FileActionException;
import com.vadimistar.cloudfilestorage.file.exception.InvalidFilePathException;
import com.vadimistar.cloudfilestorage.folder.exception.FolderActionException;
import com.vadimistar.cloudfilestorage.folder.exception.UploadFolderException;
import com.vadimistar.cloudfilestorage.index.exception.InvalidIndexPageException;
import com.vadimistar.cloudfilestorage.minio.exception.ResourceAlreadyExistsException;
import com.vadimistar.cloudfilestorage.search.exception.InvalidSearchPageException;
import com.vadimistar.cloudfilestorage.security.exception.UserNotLoggedInException;
import com.vadimistar.cloudfilestorage.common.util.path.PathUtils;
import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
        return new RedirectView("/file?path=" + URLUtils.encode(e.getPath()), true);
    }

    @ExceptionHandler(FolderActionException.class)
    public RedirectView handleFolderActionException(FolderActionException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/folder?path=" + URLUtils.encode(e.getPath()), true);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public RedirectView handleResourceNotFoundException(ResourceNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/error", true);
    }

    @ExceptionHandler(UploadFolderException.class)
    public RedirectView handleUploadFileException(UploadFolderException e, RedirectAttributes redirectAttributes) {
        String path = Objects.requireNonNullElse(e.getPath(), "");
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/?path=" + URLUtils.encode(path), true);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public RedirectView handleResourceAlreadyExistsException(ResourceAlreadyExistsException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        String parentDirectory = PathUtils.getParentDirectory(e.getPath());
        return new RedirectView("/?path=" + URLUtils.encode(parentDirectory), true);
    }

    @ExceptionHandler(InvalidIndexPageException.class)
    public RedirectView handleInvalidIndexPageException(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/error", true);
    }

    @ExceptionHandler(InvalidSearchPageException.class)
    public RedirectView handleInvalidSearchPageException(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/search", true);
    }

    @ExceptionHandler(InvalidFilePathException.class)
    public RedirectView handleInvalidFilePathException(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/error", true);
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Exception: " + e.getClass() + " message: " + e.getMessage());
        String stackTrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
        log.error(stackTrace);
        redirectAttributes.addFlashAttribute("error", "Internal error occurred, please try again later");
        return new RedirectView("/error", true);
    }
}
