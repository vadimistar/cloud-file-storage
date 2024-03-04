package com.vadimistar.cloudfilestorage;

import com.vadimistar.cloudfilestorage.common.exceptions.*;
import com.vadimistar.cloudfilestorage.common.util.PathUtils;
import com.vadimistar.cloudfilestorage.common.util.URLUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
        return new RedirectView("/file-action?path=" + URLUtils.encode(e.getPath()), true);
    }

    @ExceptionHandler(FolderActionException.class)
    public RedirectView handleFolderActionException(FolderActionException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/folder-action?path=" + URLUtils.encode(e.getPath()), true);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public RedirectView handleResourceNotFoundException(ResourceNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/", true);
    }

    @ExceptionHandler(UploadFileException.class)
    public RedirectView handleUploadFileException(UploadFileException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/?path=" + URLUtils.encode(e.getPath()), true);
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
        return new RedirectView("/", true);
    }

    @ExceptionHandler(InvalidSearchPageException.class)
    public RedirectView handleInvalidSearchPageException(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/search", true);
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleException(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Exception: " + e.getClass() + " message: " + e.getMessage());
        redirectAttributes.addFlashAttribute("error", "Internal error occurred, please try again later");
        return new RedirectView("/", true);
    }
}
