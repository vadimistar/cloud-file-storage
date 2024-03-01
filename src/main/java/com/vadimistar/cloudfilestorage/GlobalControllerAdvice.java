package com.vadimistar.cloudfilestorage;

import com.vadimistar.cloudfilestorage.exceptions.*;
import com.vadimistar.cloudfilestorage.utils.URLUtils;
import jakarta.validation.ValidationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;

@ControllerAdvice
@Log4j2
public class GlobalControllerAdvice {

    @ExceptionHandler(UserNotLoggedInException.class)
    public String handleUserNotLoggedInException(UserNotLoggedInException e) {
        return "redirect:/login";
    }

    @ExceptionHandler(FileActionException.class)
    public RedirectView handleInvalidDeleteRequestException(FileActionException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/file-action?path=" + URLUtils.encode(e.getPath()), true);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public RedirectView handleResourceNotFoundException(ResourceNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/", true);
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleException(Exception e, RedirectAttributes redirectAttributes) {
        log.error(e.getMessage());
        redirectAttributes.addFlashAttribute("error", "Internal error occurred, please try again later");
        return new RedirectView("/", true);
    }
}
