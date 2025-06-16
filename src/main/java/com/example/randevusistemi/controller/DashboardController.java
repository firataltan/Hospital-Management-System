package com.example.randevusistemi.controller;

import com.example.randevusistemi.entity.Doktor;
import com.example.randevusistemi.service.DoktorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    @Autowired
    private DoktorService doktorService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next().getAuthority();
        
        // Add user info to model
        model.addAttribute("username", auth.getName());
        model.addAttribute("role", role);
        
        // Route based on role
        switch (role) {
            case "ROLE_HASTA":
                return "hasta/dashboard";
            case "ROLE_DOKTOR":
                try {
                    Doktor doktor = doktorService.findByUsername(auth.getName())
                            .orElseThrow(() -> new RuntimeException("Doktor bulunamadı: " + auth.getName()));
                    model.addAttribute("doktor", doktor);
                } catch (RuntimeException e) {
                    redirectAttributes.addFlashAttribute("error", e.getMessage());
                    return "redirect:/login";
                }
                return "doktor/dashboard";
            case "ROLE_ADMIN":
                return "admin/dashboard";
            default:
                return "error/403";
        }
    }
} 