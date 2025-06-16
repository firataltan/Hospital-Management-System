package com.example.randevusistemi.controller;

import com.example.randevusistemi.entity.Hasta;
import com.example.randevusistemi.entity.Doktor;
import com.example.randevusistemi.service.UserService;
import com.example.randevusistemi.service.HastaService;
import com.example.randevusistemi.service.DoktorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Kimlik doğrulama işlemleri için controller
 * Login, register, logout işlemlerini yönetir
 */
@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private HastaService hastaService;
    
    @Autowired
    private DoktorService doktorService;
    
    /**
     * Ana sayfa
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Randevu Sistemi - Ana Sayfa");
        return "index";
    }
    
    /**
     * Login sayfası
     * @param error Hata parametresi
     * @param logout Logout parametresi
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        model.addAttribute("title", "Giriş Yap");
        
        if (error != null) {
            model.addAttribute("error", "Kullanıcı adı veya şifre hatalı!");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Başarıyla çıkış yapıldı.");
        }
        
        return "login";
    }
    
    /**
     * Kayıt sayfası
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "Kayıt Ol");
        model.addAttribute("hasta", new Hasta());
        model.addAttribute("doktor", new Doktor());
        return "register";
    }
    
    /**
     * Hasta kaydı işlemi
     * @param hasta Hasta form verisi
     * @param bindingResult Validation sonuçları
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/register/hasta")
    public String registerHasta(@Valid @ModelAttribute("hasta") Hasta hasta,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        
        // Validation hataları kontrolü
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Lütfen tüm alanları doğru şekilde doldurun.");
            return "redirect:/register";
        }
        
        // Kullanıcı adı kontrolü
        if (userService.existsByUsername(hasta.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Bu kullanıcı adı zaten kullanılıyor.");
            return "redirect:/register";
        }
        
        // Email kontrolü
        if (userService.existsByEmail(hasta.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Bu email adresi zaten kullanılıyor.");
            return "redirect:/register";
        }
        
        // T.C. kimlik numarası kontrolü
        if (hasta.getTcNo() != null && hastaService.existsByTcNo(hasta.getTcNo())) {
            redirectAttributes.addFlashAttribute("error", "Bu T.C. kimlik numarası zaten kayıtlı.");
            return "redirect:/register";
        }
        
        try {
            // Hasta kaydı
            hastaService.registerHasta(hasta);
            redirectAttributes.addFlashAttribute("success", "Kayıt başarılı! Şimdi giriş yapabilirsiniz.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Kayıt sırasında bir hata oluştu: " + e.getMessage());
            return "redirect:/register";
        }
    }
    
    /**
     * Doktor kaydı işlemi
     * @param doktor Doktor form verisi
     * @param bindingResult Validation sonuçları
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/register/doktor")
    public String registerDoktor(@Valid @ModelAttribute("doktor") Doktor doktor,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        
        // Validation hataları kontrolü
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Lütfen tüm alanları doğru şekilde doldurun.");
            return "redirect:/register";
        }
        
        // Kullanıcı adı kontrolü
        if (userService.existsByUsername(doktor.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Bu kullanıcı adı zaten kullanılıyor.");
            return "redirect:/register";
        }
        
        // Email kontrolü
        if (userService.existsByEmail(doktor.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Bu email adresi zaten kullanılıyor.");
            return "redirect:/register";
        }
        
        // Diploma numarası kontrolü
        if (doktor.getDiplomaNo() != null && doktorService.existsByDiplomaNo(doktor.getDiplomaNo())) {
            redirectAttributes.addFlashAttribute("error", "Bu diploma numarası zaten kayıtlı.");
            return "redirect:/register";
        }
        
        try {
            // Doktor kaydı
            doktorService.registerDoktor(doktor);
            redirectAttributes.addFlashAttribute("success", "Kayıt başarılı! Şimdi giriş yapabilirsiniz.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Kayıt sırasında bir hata oluştu: " + e.getMessage());
            return "redirect:/register";
        }
    }
    
    /**
     * Kullanıcı adı kontrolü (AJAX için)
     * @param username Kontrol edilecek kullanıcı adı
     * @return String JSON response
     */
    @GetMapping("/check-username")
    @ResponseBody
    public String checkUsername(@RequestParam("username") String username) {
        boolean exists = userService.existsByUsername(username);
        return "{\"exists\": " + exists + "}";
    }
    
    /**
     * Email kontrolü (AJAX için)
     * @param email Kontrol edilecek email
     * @return String JSON response
     */
    @GetMapping("/check-email")
    @ResponseBody
    public String checkEmail(@RequestParam("email") String email) {
        boolean exists = userService.existsByEmail(email);
        return "{\"exists\": " + exists + "}";
    }
    
    /**
     * T.C. kimlik numarası kontrolü (AJAX için)
     * @param tcNo Kontrol edilecek T.C. numarası
     * @return String JSON response
     */
    @GetMapping("/check-tcno")
    @ResponseBody
    public String checkTcNo(@RequestParam("tcNo") String tcNo) {
        boolean exists = hastaService.existsByTcNo(tcNo);
        return "{\"exists\": " + exists + "}";
    }
    
    /**
     * Diploma numarası kontrolü (AJAX için)
     * @param diplomaNo Kontrol edilecek diploma numarası
     * @return String JSON response
     */
    @GetMapping("/check-diploma")
    @ResponseBody
    public String checkDiplomaNo(@RequestParam("diplomaNo") String diplomaNo) {
        boolean exists = doktorService.existsByDiplomaNo(diplomaNo);
        return "{\"exists\": " + exists + "}";
    }
} 