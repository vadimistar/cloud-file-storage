package com.vadimistar.cloudfilestorage.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorViewController implements ErrorController {

    @GetMapping
    public String error(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (!model.containsAttribute("error")) {
            model.addAttribute("error", getErrorMessage(status));
        }
        return "error";
    }

    private static String getErrorMessage(Object status) {
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "Not found";
            }
        }

        return "Internal error occurred, please try again later";
    }
}
