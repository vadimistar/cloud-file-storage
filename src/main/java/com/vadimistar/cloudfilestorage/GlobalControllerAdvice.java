package com.vadimistar.cloudfilestorage;

import com.vadimistar.cloudfilestorage.exceptions.FileServiceException;
import com.vadimistar.cloudfilestorage.exceptions.ResourceNotFoundException;
import com.vadimistar.cloudfilestorage.exceptions.UserNotLoggedInException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(UserNotLoggedInException.class)
    public String handleUserNotLoggedInException(UserNotLoggedInException e) {
        return "redirect:/login";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public RedirectView handleResourceNotFoundException(ResourceNotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());

        return new RedirectView("/error", true);
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleException(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());

        return new RedirectView("/error", true);
    }
}
