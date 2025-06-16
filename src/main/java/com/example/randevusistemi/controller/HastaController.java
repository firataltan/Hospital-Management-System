package com.example.randevusistemi.controller;

import com.example.randevusistemi.entity.Hasta;
import com.example.randevusistemi.entity.Doktor;
import com.example.randevusistemi.entity.Randevu;
import com.example.randevusistemi.service.HastaService;
import com.example.randevusistemi.service.DoktorService;
import com.example.randevusistemi.service.RandevuService;
import com.example.randevusistemi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Hasta işlemleri için controller sınıfı
 * Hasta dashboard, randevu alma ve randevu yönetimi
 */
@Controller
@RequestMapping("/hasta")
public class HastaController {
    
    @Autowired
    private HastaService hastaService;
    
    @Autowired
    private DoktorService doktorService;
    
    @Autowired
    private RandevuService randevuService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Giriş yapmış hastayı getirir
     * @return Hasta giriş yapmış hasta objesi
     */
    private Hasta getCurrentHasta() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return hastaService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Hasta bulunamadı: " + username));
    }
    
    /**
     * Hasta dashboard sayfası
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Hasta hasta = getCurrentHasta();
        
        // Hasta bilgileri
        model.addAttribute("hasta", hasta);
        model.addAttribute("title", "Hasta Paneli - " + hasta.getFullName());
        
        // Randevu istatistikleri
        List<Randevu> tumRandevular = hastaService.getHastaRandevulari(hasta);
        List<Randevu> gelecekRandevular = hastaService.getHastaGelecekRandevulari(hasta);
        List<Randevu> gecmisRandevular = hastaService.getHastaGecmisRandevulari(hasta);
        
        model.addAttribute("toplamRandevuSayisi", tumRandevular.size());
        model.addAttribute("gelecekRandevuSayisi", gelecekRandevular.size());
        model.addAttribute("gecmisRandevuSayisi", gecmisRandevular.size());
        
        // Son 5 randevu
        List<Randevu> sonRandevular = tumRandevular.stream()
                .limit(5)
                .toList();
        model.addAttribute("sonRandevular", sonRandevular);
        
        // Bir sonraki randevu
        Optional<Randevu> sonrakiRandevu = gelecekRandevular.stream()
                .findFirst();
        if (sonrakiRandevu.isPresent()) {
            model.addAttribute("sonrakiRandevu", sonrakiRandevu.get());
        }
        
        return "hasta/dashboard";
    }
    
    /**
     * Randevu alma sayfası
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/randevu-al")
    public String randevuAl(Model model) {
        Hasta hasta = getCurrentHasta();
        
        model.addAttribute("hasta", hasta);
        model.addAttribute("title", "Randevu Al");
        
        // Müsait doktorları getir
        List<Doktor> musaitDoktorlar = doktorService.findAvailableDoctors();
        model.addAttribute("doktorlar", musaitDoktorlar);
        
        // Uzmanlık alanları
        List<String> uzmanlikAlanlari = doktorService.getAllSpecializations();
        model.addAttribute("uzmanlikAlanlari", uzmanlikAlanlari);
        
        // Hastaneler
        List<String> hastaneler = doktorService.getAllHospitals();
        model.addAttribute("hastaneler", hastaneler);
        
        return "hasta/randevu-al";
    }
    
    /**
     * Doktor filtreleme (AJAX)
     * @param specialization Uzmanlık alanı
     * @param hospital Hastane
     * @param city Şehir
     * @return String JSON response
     */
    @GetMapping("/filter-doktorlar")
    @ResponseBody
    public List<Doktor> filterDoktorlar(@RequestParam(required = false) String specialization,
                                       @RequestParam(required = false) String hospital,
                                       @RequestParam(required = false) String city) {
        
        List<Doktor> doktorlar = doktorService.findAvailableDoctors();
        
        // Filtreleme işlemleri
        if (specialization != null && !specialization.isEmpty()) {
            doktorlar = doktorlar.stream()
                    .filter(d -> d.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .toList();
        }
        
        if (hospital != null && !hospital.isEmpty()) {
            doktorlar = doktorlar.stream()
                    .filter(d -> d.getHospital() != null && 
                                d.getHospital().toLowerCase().contains(hospital.toLowerCase()))
                    .toList();
        }
        
        if (city != null && !city.isEmpty()) {
            doktorlar = doktorService.findByCity(city);
        }
        
        return doktorlar;
    }
    
    /**
     * Doktor müsait saatleri getir (AJAX)
     * @param doktorId Doktor ID
     * @param date Tarih
     * @return List<String> müsait saatler
     */
    @GetMapping("/musait-saatler")
    @ResponseBody
    public List<String> getMusaitSaatler(@RequestParam Long doktorId, 
                                        @RequestParam String date) {
        
        Optional<Doktor> doktorOpt = doktorService.findById(doktorId);
        if (doktorOpt.isEmpty()) {
            return List.of();
        }
        
        Doktor doktor = doktorOpt.get();
        LocalDate selectedDate = LocalDate.parse(date);
        
        // Geçmiş tarih kontrolü
        if (selectedDate.isBefore(LocalDate.now())) {
            return List.of();
        }
        
        List<LocalTime> musaitSaatler = randevuService.getAvailableSlots(doktor, selectedDate);
        
        // LocalTime'ı String'e çevir
        return musaitSaatler.stream()
                .map(time -> time.format(DateTimeFormatter.ofPattern("HH:mm")))
                .toList();
    }
    
    /**
     * Randevu alma işlemi
     * @param doktorId Doktor ID
     * @param appointmentDate Randevu tarihi
     * @param appointmentTime Randevu saati
     * @param complaint Şikayet
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/randevu-al")
    public String randevuAlPost(@RequestParam Long doktorId,
                               @RequestParam String appointmentDate,
                               @RequestParam String appointmentTime,
                               @RequestParam String complaint,
                               RedirectAttributes redirectAttributes) {
        
        try {
            Hasta hasta = getCurrentHasta();
            
            // Doktor kontrolü
            Optional<Doktor> doktorOpt = doktorService.findById(doktorId);
            if (doktorOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Doktor bulunamadı.");
                return "redirect:/hasta/randevu-al";
            }
            
            Doktor doktor = doktorOpt.get();
            
            // Tarih ve saat birleştirme
            LocalDate date = LocalDate.parse(appointmentDate);
            LocalTime time = LocalTime.parse(appointmentTime);
            LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);
            
            // Randevu oluştur
            Randevu randevu = randevuService.createRandevu(hasta, doktor, appointmentDateTime, complaint);
            
            redirectAttributes.addFlashAttribute("success", 
                "Randevunuz başarıyla alındı. Randevu No: " + randevu.getId() + 
                ". Doktor onayını beklemektedir.");
            
            return "redirect:/hasta/randevularim";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/hasta/randevu-al";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Randevu alınırken bir hata oluştu: " + e.getMessage());
            return "redirect:/hasta/randevu-al";
        }
    }
    
    /**
     * Hasta randevuları sayfası
     * @param status Durum filtresi (opsiyonel)
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/randevularim")
    public String randevularim(@RequestParam(required = false) String status, Model model) {
        Hasta hasta = getCurrentHasta();
        
        model.addAttribute("hasta", hasta);
        model.addAttribute("title", "Randevularım");
        
        List<Randevu> randevular;
        
        // Durum filtresine göre randevuları getir
        if ("gelecek".equals(status)) {
            randevular = hastaService.getHastaGelecekRandevulari(hasta);
            model.addAttribute("activeFilter", "gelecek");
        } else if ("gecmis".equals(status)) {
            randevular = hastaService.getHastaGecmisRandevulari(hasta);
            model.addAttribute("activeFilter", "gecmis");
        } else {
            randevular = hastaService.getHastaRandevulari(hasta);
            model.addAttribute("activeFilter", "tumu");
        }
        
        model.addAttribute("randevular", randevular);
        
        // İstatistikler
        List<Randevu> tumRandevular = hastaService.getHastaRandevulari(hasta);
        List<Randevu> gelecekRandevular = hastaService.getHastaGelecekRandevulari(hasta);
        List<Randevu> gecmisRandevular = hastaService.getHastaGecmisRandevulari(hasta);
        
        model.addAttribute("toplamSayi", tumRandevular.size());
        model.addAttribute("gelecekSayi", gelecekRandevular.size());
        model.addAttribute("gecmisSayi", gecmisRandevular.size());
        
        return "hasta/randevularim";
    }
    
    /**
     * Randevu detay sayfası
     * @param id Randevu ID
     * @param model Thymeleaf model
     * @param redirectAttributes Redirect attributes
     * @return String template adı veya redirect URL'i
     */
    @GetMapping("/randevu/{id}")
    public String randevuDetay(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Hasta hasta = getCurrentHasta();
        
        Optional<Randevu> randevuOpt = randevuService.findById(id);
        if (randevuOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Randevu bulunamadı.");
            return "redirect:/hasta/randevularim";
        }
        
        Randevu randevu = randevuOpt.get();
        
        // Hastanın randevusu mu kontrol et
        if (!randevu.getHasta().getId().equals(hasta.getId())) {
            redirectAttributes.addFlashAttribute("error", "Bu randevuyu görüntüleme yetkiniz yok.");
            return "redirect:/hasta/randevularim";
        }
        
        model.addAttribute("hasta", hasta);
        model.addAttribute("randevu", randevu);
        model.addAttribute("title", "Randevu Detayı - " + randevu.getId());
        
        return "hasta/randevu-detay";
    }
    
    /**
     * Randevu iptal etme
     * @param id Randevu ID
     * @param reason İptal sebebi
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/randevu/{id}/iptal")
    public String randevuIptal(@PathVariable Long id,
                              @RequestParam String reason,
                              RedirectAttributes redirectAttributes) {
        
        Hasta hasta = getCurrentHasta();
        
        Optional<Randevu> randevuOpt = randevuService.findById(id);
        if (randevuOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Randevu bulunamadı.");
            return "redirect:/hasta/randevularim";
        }
        
        Randevu randevu = randevuOpt.get();
        
        // Hastanın randevusu mu kontrol et
        if (!randevu.getHasta().getId().equals(hasta.getId())) {
            redirectAttributes.addFlashAttribute("error", "Bu randevuyu iptal etme yetkiniz yok.");
            return "redirect:/hasta/randevularim";
        }
        
        // Randevu iptal edilebilir mi kontrol et
        if (!randevu.canBeCancelled()) {
            redirectAttributes.addFlashAttribute("error", "Bu randevu iptal edilemez.");
            return "redirect:/hasta/randevu/" + id;
        }
        
        // İptal işlemi
        boolean success = randevuService.cancelRandevu(id, reason);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Randevunuz başarıyla iptal edildi.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Randevu iptal edilirken bir hata oluştu.");
        }
        
        return "redirect:/hasta/randevularim";
    }
    
    /**
     * Hasta profil sayfası
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/profil")
    public String profil(Model model) {
        Hasta hasta = getCurrentHasta();
        
        model.addAttribute("hasta", hasta);
        model.addAttribute("title", "Profilim");
        
        return "hasta/profil";
    }
    
    /**
     * Hasta profil güncelleme
     * @param hasta Güncellenmiş hasta bilgileri
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/profil")
    public String profilGuncelle(@ModelAttribute Hasta hasta,
                                RedirectAttributes redirectAttributes) {
        
        try {
            Hasta mevcutHasta = getCurrentHasta();
            
            // Güncellenebilir alanları ayarla
            mevcutHasta.setFirstName(hasta.getFirstName());
            mevcutHasta.setLastName(hasta.getLastName());
            mevcutHasta.setEmail(hasta.getEmail());
            mevcutHasta.setPhone(hasta.getPhone());
            mevcutHasta.setBirthDate(hasta.getBirthDate());
            mevcutHasta.setGender(hasta.getGender());
            mevcutHasta.setAddress(hasta.getAddress());
            mevcutHasta.setEmergencyContact(hasta.getEmergencyContact());
            mevcutHasta.setEmergencyPhone(hasta.getEmergencyPhone());
            
            // Email değişmişse kontrolü
            if (!mevcutHasta.getEmail().equals(hasta.getEmail()) && 
                userService.existsByEmail(hasta.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Bu email adresi zaten kullanılıyor.");
                return "redirect:/hasta/profil";
            }
            
            hastaService.updateProfile(mevcutHasta);
            redirectAttributes.addFlashAttribute("success", "Profiliniz başarıyla güncellendi.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Profil güncellenirken bir hata oluştu: " + e.getMessage());
        }
        
        return "redirect:/hasta/profil";
    }
    
    /**
     * Şifre değiştirme sayfası
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/sifre-degistir")
    public String sifreDegistir(Model model) {
        Hasta hasta = getCurrentHasta();
        
        model.addAttribute("hasta", hasta);
        model.addAttribute("title", "Şifre Değiştir");
        
        return "hasta/sifre-degistir";
    }
    
    /**
     * Şifre değiştirme işlemi
     * @param currentPassword Mevcut şifre
     * @param newPassword Yeni şifre
     * @param confirmPassword Yeni şifre tekrar
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/sifre-degistir")
    public String sifreDegistirPost(@RequestParam String currentPassword,
                                   @RequestParam String newPassword,
                                   @RequestParam String confirmPassword,
                                   RedirectAttributes redirectAttributes) {
        
        try {
            Hasta hasta = getCurrentHasta();
            
            // Mevcut şifre kontrolü
            if (!userService.checkPassword(currentPassword, hasta.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mevcut şifreniz hatalı.");
                return "redirect:/hasta/sifre-degistir";
            }
            
            // Yeni şifre kontrolleri
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Yeni şifre en az 6 karakter olmalıdır.");
                return "redirect:/hasta/sifre-degistir";
            }
            
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Yeni şifreler eşleşmiyor.");
                return "redirect:/hasta/sifre-degistir";
            }
            
            // Şifre güncelleme
            userService.updatePassword(hasta, newPassword);
            redirectAttributes.addFlashAttribute("success", "Şifreniz başarıyla güncellendi.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Şifre değiştirilirken bir hata oluştu: " + e.getMessage());
        }
        
        return "redirect:/hasta/profil";
    }
} 