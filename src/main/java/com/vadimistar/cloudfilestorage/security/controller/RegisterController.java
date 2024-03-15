package com.vadimistar.cloudfilestorage.security.controller;

import com.vadimistar.cloudfilestorage.security.exception.RegisterUserException;
import com.vadimistar.cloudfilestorage.security.dto.RegisterUserRequestDto;
import com.vadimistar.cloudfilestorage.security.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;

    @GetMapping
    public String register(Model model) {
        model.addAttribute("user", RegisterUserRequestDto.builder().build());
        return "register";
    }

    @PostMapping
    public String register(@ModelAttribute("user") @Valid RegisterUserRequestDto registerUserRequestDto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", registerUserRequestDto);
            return "register";
        }
        try {
            userService.registerUser(registerUserRequestDto);
        } catch (RegisterUserException e) {
            model.addAttribute("user", registerUserRequestDto);
            bindingResult.addError(new ObjectError("error", e.getMessage()));
            return "register";
        }
        return "redirect:/login?registered";
    }

    private static final String ROOT_DIRECTORY = "/";
}
