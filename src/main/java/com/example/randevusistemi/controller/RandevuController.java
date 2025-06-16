package com.example.randevusistemi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RandevuController {
    // Randevu oluşturma işlemi HastaController'da yapılıyor
    
    /**
     * Eski randevu oluşturma endpoint'ini yeni endpoint'e yönlendirir
     */
    @GetMapping("/randevu/olustur")
    public String redirectToNewEndpoint() {
        return "redirect:/hasta/randevu-al";
    }
} 