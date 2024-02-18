package com.vadimistar.cloudfilestorage.controllers;

import com.vadimistar.cloudfilestorage.dto.RegisterDto;
import com.vadimistar.cloudfilestorage.exceptions.PasswordMismatchException;
import com.vadimistar.cloudfilestorage.exceptions.UserAlreadyExistsException;
import com.vadimistar.cloudfilestorage.services.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@AllArgsConstructor
public class RegisterController {

    private final UserService userService;

    @GetMapping("/register")
    public String registerView(Model model) {
        model.addAttribute("user", RegisterDto.builder().build());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") @Valid RegisterDto registerDto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", registerDto);
            return "register";
        }

        try {
            userService.registerUser(registerDto);
        } catch (UserAlreadyExistsException | PasswordMismatchException e) {
            model.addAttribute("user", registerDto);
            bindingResult.addError(new ObjectError("error", e.getMessage()));
            return "register";
        }

        return "redirect:/login?registered";
    }
}
