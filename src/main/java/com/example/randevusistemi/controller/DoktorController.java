package com.example.randevusistemi.controller;

import com.example.randevusistemi.entity.Doktor;
import com.example.randevusistemi.entity.Randevu;
import com.example.randevusistemi.enums.RandevuDurumu;
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
 * Doktor işlemleri için controller sınıfı
 * Doktor dashboard, randevu yönetimi ve profil işlemleri
 */
@Controller
@RequestMapping("/doktor")
public class DoktorController {
    
    @Autowired
    private DoktorService doktorService;
    
    @Autowired
    private RandevuService randevuService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Giriş yapmış doktoru getirir
     * @return Doktor giriş yapmış doktor objesi
     */
    private Doktor getCurrentDoktor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return doktorService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doktor bulunamadı: " + username));
    }
    
    /**
     * Doktor dashboard sayfası
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, RedirectAttributes redirectAttributes) {
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
        
        // Doktor bilgileri
        model.addAttribute("doktor", doktor);
        model.addAttribute("title", "Doktor Paneli - " + doktor.getDoctorTitle());
        
        // Randevu istatistikleri
        List<Randevu> tumRandevular = doktorService.getDoktorRandevulari(doktor);
        List<Randevu> bekleyenRandevular = doktorService.getDoktorBekleyenRandevulari(doktor);
        List<Randevu> bugunRandevular = doktorService.getDoktorBugunRandevulari(doktor);
        
        model.addAttribute("toplamRandevuSayisi", tumRandevular.size());
        model.addAttribute("bekleyenRandevuSayisi", bekleyenRandevular.size());
        model.addAttribute("bugunRandevuSayisi", bugunRandevular.size());
        
        // Durum bazlı istatistikler
        long onaylananSayi = tumRandevular.stream()
                .filter(r -> r.getStatus() == RandevuDurumu.ONAYLANDI)
                .count();
        long reddedilenSayi = tumRandevular.stream()
                .filter(r -> r.getStatus() == RandevuDurumu.REDDEDILDI)
                .count();
        
        model.addAttribute("onaylananSayi", onaylananSayi);
        model.addAttribute("reddedilenSayi", reddedilenSayi);
        
        // Bugünkü randevular (maksimum 5 tanesi)
        List<Randevu> bugunRandevularLimited = bugunRandevular.stream()
                .limit(5)
                .toList();
        model.addAttribute("bugunRandevular", bugunRandevularLimited);
        
        // Bekleyen randevular (maksimum 5 tanesi)
        List<Randevu> bekleyenRandevularLimited = bekleyenRandevular.stream()
                .limit(5)
                .toList();
        model.addAttribute("bekleyenRandevular", bekleyenRandevularLimited);
        
        // Bir sonraki randevu
        Optional<Randevu> sonrakiRandevu = tumRandevular.stream()
                .filter(r -> r.getAppointmentDateTime().isAfter(LocalDateTime.now()))
                .filter(r -> r.getStatus() == RandevuDurumu.ONAYLANDI)
                .filter(r -> !r.getCancelled())
                .findFirst();
        
        if (sonrakiRandevu.isPresent()) {
            model.addAttribute("sonrakiRandevu", sonrakiRandevu.get());
        }
        
        return "doktor/dashboard";
    }
    
    /**
     * Randevu yönetimi sayfası
     * @param status Durum filtresi (opsiyonel)
     * @param date Tarih filtresi (opsiyonel)
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/randevular")
    public String randevular(@RequestParam(required = false) String status,
                            @RequestParam(required = false) String date,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
        
        model.addAttribute("doktor", doktor);
        model.addAttribute("title", "Randevu Yönetimi");
        
        List<Randevu> randevular = doktorService.getDoktorRandevulari(doktor);
        
        // Durum filtresine göre randevuları filtrele
        if ("bekleyen".equals(status)) {
            randevular = randevular.stream()
                    .filter(r -> r.getStatus() == RandevuDurumu.BEKLEMEDE)
                    .filter(r -> !r.getCancelled())
                    .toList();
            model.addAttribute("activeFilter", "bekleyen");
        } else if ("onaylanan".equals(status)) {
            randevular = randevular.stream()
                    .filter(r -> r.getStatus() == RandevuDurumu.ONAYLANDI)
                    .toList();
            model.addAttribute("activeFilter", "onaylanan");
        } else if ("reddedilen".equals(status)) {
            randevular = randevular.stream()
                    .filter(r -> r.getStatus() == RandevuDurumu.REDDEDILDI)
                    .toList();
            model.addAttribute("activeFilter", "reddedilen");
        } else if ("bugun".equals(status)) {
            LocalDate today = LocalDate.now();
            randevular = randevular.stream()
                    .filter(r -> r.getAppointmentDateTime().toLocalDate().equals(today))
                    .filter(r -> !r.getCancelled())
                    .toList();
            model.addAttribute("activeFilter", "bugun");
        } else {
            // Tüm randevular ama iptal edilmemişler
            randevular = randevular.stream()
                    .filter(r -> !r.getCancelled())
                    .toList();
            model.addAttribute("activeFilter", "tumu");
        }
        
        // Tarih filtresine göre randevuları filtrele
        if (date != null && !date.isEmpty()) {
            LocalDate filterDate = LocalDate.parse(date);
            randevular = randevular.stream()
                    .filter(r -> r.getAppointmentDateTime().toLocalDate().equals(filterDate))
                    .toList();
            model.addAttribute("selectedDate", date);
        }
        
        model.addAttribute("randevular", randevular);
        
        // İstatistikler
        List<Randevu> tumRandevular = doktorService.getDoktorRandevulari(doktor);
        long bekleyenSayi = tumRandevular.stream()
                .filter(r -> r.getStatus() == RandevuDurumu.BEKLEMEDE && !r.getCancelled())
                .count();
        long onaylananSayi = tumRandevular.stream()
                .filter(r -> r.getStatus() == RandevuDurumu.ONAYLANDI)
                .count();
        long reddedilenSayi = tumRandevular.stream()
                .filter(r -> r.getStatus() == RandevuDurumu.REDDEDILDI)
                .count();
        long bugunSayi = doktorService.getDoktorBugunRandevulari(doktor).size();
        
        model.addAttribute("bekleyenSayi", bekleyenSayi);
        model.addAttribute("onaylananSayi", onaylananSayi);
        model.addAttribute("reddedilenSayi", reddedilenSayi);
        model.addAttribute("bugunSayi", bugunSayi);
        model.addAttribute("toplamSayi", tumRandevular.size());
        
        return "doktor/randevular";
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
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
        
        Optional<Randevu> randevuOpt = randevuService.findById(id);
        if (randevuOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Randevu bulunamadı.");
            return "redirect:/doktor/randevular";
        }
        
        Randevu randevu = randevuOpt.get();
        
        // Doktorun randevusu mu kontrol et
        if (!randevu.getDoktor().getId().equals(doktor.getId())) {
            redirectAttributes.addFlashAttribute("error", "Bu randevuyu görüntüleme yetkiniz yok.");
            return "redirect:/doktor/randevular";
        }
        
        model.addAttribute("doktor", doktor);
        model.addAttribute("randevu", randevu);
        model.addAttribute("title", "Randevu Detayı - " + randevu.getId());
        
        return "doktor/randevu-detay";
    }
    
    /**
     * Randevu onaylama işlemi
     * @param id Randevu ID
     * @param doctorNotes Doktor notları (opsiyonel)
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/randevu/{id}/onayla")
    public String approveRandevu(@PathVariable Long id,
                                 @RequestParam(required = false) String doctorNotes,
                                 RedirectAttributes redirectAttributes) {
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }

        boolean success = randevuService.approveRandevu(id, doctorNotes);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Randevu başarıyla onaylandı.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Randevu onaylanırken bir hata oluştu veya zaten onaylanmış/reddedilmiş.");
        }
        return "redirect:/doktor/randevular";
    }
    
    /**
     * Randevu reddetme işlemi
     * @param id Randevu ID
     * @param reason Reddetme sebebi
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/randevu/{id}/reddet")
    public String rejectRandevu(@PathVariable Long id,
                                @RequestParam String reason,
                                RedirectAttributes redirectAttributes) {
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }

        boolean success = randevuService.rejectRandevu(id, reason);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Randevu başarıyla reddedildi.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Randevu reddedilirken bir hata oluştu veya zaten onaylanmış/reddedilmiş.");
        }
        return "redirect:/doktor/randevular";
    }
    
    /**
     * Doktor notu ekleme/güncelleme işlemi
     * @param id Randevu ID
     * @param notes Doktor notları
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/randevu/{id}/not-ekle")
    public String addDoctorNotes(@PathVariable Long id,
                                @RequestParam String notes,
                                RedirectAttributes redirectAttributes) {
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }

        boolean success = randevuService.addDoctorNotes(id, notes);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Doktor notları başarıyla kaydedildi.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Doktor notları kaydedilirken bir hata oluştu.");
        }
        return "redirect:/doktor/randevular";
    }
    
    /**
     * Doktor profil sayfası
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/profil")
    public String profil(Model model, RedirectAttributes redirectAttributes) {
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
        
        model.addAttribute("doktor", doktor);
        model.addAttribute("title", "Profilim");
        
        return "doktor/profil";
    }
    
    /**
     * Doktor profilini güncelleme işlemi
     * @param updatedDoktor Güncellenmiş doktor objesi
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/profil")
    public String updateProfil(@ModelAttribute Doktor updatedDoktor, RedirectAttributes redirectAttributes) {
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }

        doktor.setFirstName(updatedDoktor.getFirstName());
        doktor.setLastName(updatedDoktor.getLastName());
        doktor.setEmail(updatedDoktor.getEmail());
        doktor.setPhone(updatedDoktor.getPhone());
        doktor.setSpecialization(updatedDoktor.getSpecialization());
        doktor.setHospital(updatedDoktor.getHospital());
        doktor.setClinicAddress(updatedDoktor.getClinicAddress());
        doktor.setExperienceYears(updatedDoktor.getExperienceYears());
        doktor.setConsultationFee(updatedDoktor.getConsultationFee());
        doktor.setAbout(updatedDoktor.getAbout());

        doktorService.updateProfile(doktor);
        redirectAttributes.addFlashAttribute("success", "Profiliniz başarıyla güncellendi!");
        return "redirect:/doktor/profil";
    }
    
    /**
     * Çalışma saatleri sayfası
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/calisma-saatleri")
    public String calismaSaatleri(Model model, RedirectAttributes redirectAttributes) {
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }

        model.addAttribute("doktor", doktor);
        model.addAttribute("title", "Çalışma Saatleri");
        
        return "doktor/calisma-saatleri";
    }

    /**
     * Çalışma saatlerini güncelleme işlemi
     * @param workStartTime Başlangıç saati
     * @param workEndTime Bitiş saati
     * @param appointmentDuration Randevu süresi
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/calisma-saatleri")
    public String updateCalismaSaatleri(@RequestParam String workStartTime,
                                        @RequestParam String workEndTime,
                                        @RequestParam Integer appointmentDuration,
                                        RedirectAttributes redirectAttributes) {
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }

        try {
            LocalTime start = LocalTime.parse(workStartTime);
            LocalTime end = LocalTime.parse(workEndTime);
            
            if (start.isAfter(end)) {
                redirectAttributes.addFlashAttribute("error", "Başlangıç saati bitiş saatinden sonra olamaz.");
                return "redirect:/doktor/calisma-saatleri";
            }

            doktor.setWorkStartTime(start);
            doktor.setWorkEndTime(end);
            doktor.setAppointmentDuration(appointmentDuration);
            doktorService.save(doktor);
            redirectAttributes.addFlashAttribute("success", "Çalışma saatleri başarıyla güncellendi!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Çalışma saatleri güncellenirken bir hata oluştu: " + e.getMessage());
        }
        return "redirect:/doktor/calisma-saatleri";
    }

    /**
     * Şifre değiştirme sayfası
     * @param model Thymeleaf model
     * @return String template adı
     */
    @GetMapping("/sifre-degistir")
    public String sifreDegistir(Model model, RedirectAttributes redirectAttributes) {
        try {
            getCurrentDoktor(); // Sadece doktorun varlığını kontrol et
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
        model.addAttribute("title", "Şifre Değiştir");
        return "doktor/sifre-degistir";
    }

    /**
     * Şifre değiştirme işlemi
     * @param currentPassword Mevcut şifre
     * @param newPassword Yeni şifre
     * @param newPasswordConfirm Yeni şifre tekrarı
     * @param redirectAttributes Redirect attributes
     * @return String redirect URL'i
     */
    @PostMapping("/sifre-degistir")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String newPasswordConfirm,
                                 RedirectAttributes redirectAttributes) {
        Doktor doktor;
        try {
            doktor = getCurrentDoktor();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            redirectAttributes.addFlashAttribute("error", "Yeni şifreler eşleşmiyor!");
            return "redirect:/doktor/sifre-degistir";
        }

        if (!userService.checkPassword(currentPassword, doktor.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mevcut şifreniz yanlış!");
            return "redirect:/doktor/sifre-degistir";
        }

        userService.updatePassword(doktor, newPassword);
        redirectAttributes.addFlashAttribute("success", "Şifreniz başarıyla değiştirildi!");
        return "redirect:/doktor/profil";
    }
} 