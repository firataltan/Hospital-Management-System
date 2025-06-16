package com.example.randevusistemi.service;

import com.example.randevusistemi.entity.Randevu;
import com.example.randevusistemi.entity.Hasta;
import com.example.randevusistemi.entity.Doktor;
import com.example.randevusistemi.enums.RandevuDurumu;
import com.example.randevusistemi.repository.RandevuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Randevu yönetimi için service sınıfı
 * Randevu CRUD işlemleri ve iş mantığını yönetir
 */
@Service
@Transactional
public class RandevuService {
    
    @Autowired
    private RandevuRepository randevuRepository;
    
    /**
     * ID'ye göre randevu bulur
     * @param id Randevu ID
     * @return Optional<Randevu> randevu varsa Randevu objesi
     */
    public Optional<Randevu> findById(Long id) {
        return randevuRepository.findById(id);
    }
    
    /**
     * Hastaya ait randevuları getirir
     * @param hasta Hasta objesi
     * @return List<Randevu> hastanın randevuları (tarih sıralı)
     */
    public List<Randevu> findByHasta(Hasta hasta) {
        return randevuRepository.findByHastaOrderByAppointmentDateTimeDesc(hasta);
    }
    
    /**
     * Doktora ait randevuları getirir
     * @param doktor Doktor objesi
     * @return List<Randevu> doktorun randevuları (tarih sıralı)
     */
    public List<Randevu> findByDoktor(Doktor doktor) {
        return randevuRepository.findByDoktorOrderByAppointmentDateTimeAsc(doktor);
    }
    
    /**
     * Hastanın gelecek randevularını getirir
     * @param hasta Hasta objesi
     * @return List<Randevu> hastanın gelecek randevuları
     */
    public List<Randevu> findByHastaGelecek(Hasta hasta) {
        return randevuRepository.findByHastaAndAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(
                hasta, LocalDateTime.now());
    }
    
    /**
     * Hastanın geçmiş randevularını getirir
     * @param hasta Hasta objesi
     * @return List<Randevu> hastanın geçmiş randevuları
     */
    public List<Randevu> findByHastaGecmis(Hasta hasta) {
        return randevuRepository.findByHastaAndAppointmentDateTimeBeforeOrderByAppointmentDateTimeDesc(
                hasta, LocalDateTime.now());
    }
    
    /**
     * Doktorun gelecek randevularını getirir
     * @param doktor Doktor objesi
     * @return List<Randevu> doktorun gelecek randevuları
     */
    public List<Randevu> findByDoktorGelecek(Doktor doktor) {
        return randevuRepository.findByDoktorAndAppointmentDateTimeBetween(
                doktor, LocalDateTime.now(), LocalDateTime.now().plusYears(1));
    }
    
    /**
     * Doktorun bekleyen randevularını getirir
     * @param doktor Doktor objesi
     * @return List<Randevu> onay bekleyen randevular
     */
    public List<Randevu> findByDoktorBekleyen(Doktor doktor) {
        return randevuRepository.findPendingAppointments(doktor);
    }
    
    /**
     * Doktorun bugünkü randevularını getirir
     * @param doktor Doktor objesi
     * @return List<Randevu> bugünkü randevular
     */
    public List<Randevu> findByDoktorBugun(Doktor doktor) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return randevuRepository.findTodayAppointments(doktor, startOfDay, endOfDay);
    }
    
    /**
     * Duruma göre randevuları getirir
     * @param status Randevu durumu
     * @return List<Randevu> belirtilen durumdaki randevular
     */
    public List<Randevu> findByStatus(RandevuDurumu status) {
        return randevuRepository.findByStatus(status);
    }
    
    /**
     * Yeni randevu oluşturur
     * @param hasta Hasta objesi
     * @param doktor Doktor objesi
     * @param appointmentDateTime Randevu tarihi ve saati
     * @param complaint Hasta şikayeti
     * @return Randevu oluşturulan randevu objesi
     * @throws IllegalArgumentException Çakışma varsa veya geçersiz tarih
     */
    public Randevu createRandevu(Hasta hasta, Doktor doktor, LocalDateTime appointmentDateTime, String complaint) {
        // Geçmiş tarih kontrolü
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Geçmiş tarih için randevu alınamaz");
        }
        
        // Doktor çalışma saati kontrolü
        if (!isDoctorAvailable(doktor, appointmentDateTime)) {
            throw new IllegalArgumentException("Doktor bu saatte müsait değil");
        }
        
        // Çakışma kontrolü
        if (hasConflictingAppointment(doktor, appointmentDateTime)) {
            throw new IllegalArgumentException("Bu saatte başka bir randevu mevcut");
        }
        
        // Doktor randevu kabul ediyor mu kontrolü
        if (!doktor.getAcceptingAppointments()) {
            throw new IllegalArgumentException("Doktor şu anda randevu kabul etmiyor");
        }
        
        // Randevu oluştur
        Randevu randevu = new Randevu(hasta, doktor, appointmentDateTime, complaint);
        return randevuRepository.save(randevu);
    }
    
    /**
     * Randevuyu onaylar
     * @param randevuId Randevu ID
     * @param doctorNotes Doktor notları (opsiyonel)
     * @return boolean işlem başarılı mı
     */
    public boolean approveRandevu(Long randevuId, String doctorNotes) {
        Optional<Randevu> randevuOpt = randevuRepository.findById(randevuId);
        if (randevuOpt.isPresent()) {
            Randevu randevu = randevuOpt.get();
            
            if (randevu.getStatus() != RandevuDurumu.BEKLEMEDE) {
                return false; // Sadece beklemedeki randevular onaylanabilir
            }
            
            if (randevu.getCancelled()) {
                return false; // İptal edilmiş randevular onaylanamaz
            }
            
            randevu.setStatus(RandevuDurumu.ONAYLANDI);
            if (doctorNotes != null && !doctorNotes.trim().isEmpty()) {
                randevu.setDoctorNotes(doctorNotes);
            }
            
            randevuRepository.save(randevu);
            return true;
        }
        return false;
    }
    
    /**
     * Randevuyu reddeder
     * @param randevuId Randevu ID
     * @param reason Red sebebi
     * @return boolean işlem başarılı mı
     */
    public boolean rejectRandevu(Long randevuId, String reason) {
        Optional<Randevu> randevuOpt = randevuRepository.findById(randevuId);
        if (randevuOpt.isPresent()) {
            Randevu randevu = randevuOpt.get();
            
            if (randevu.getStatus() != RandevuDurumu.BEKLEMEDE) {
                return false; // Sadece beklemedeki randevular reddedilebilir
            }
            
            if (randevu.getCancelled()) {
                return false; // İptal edilmiş randevular reddedilemez
            }
            
            randevu.setStatus(RandevuDurumu.REDDEDILDI);
            if (reason != null && !reason.trim().isEmpty()) {
                randevu.setDoctorNotes(reason);
            }
            
            randevuRepository.save(randevu);
            return true;
        }
        return false;
    }
    
    /**
     * Randevuyu iptal eder
     * @param randevuId Randevu ID
     * @param reason İptal sebebi
     * @return boolean işlem başarılı mı
     */
    public boolean cancelRandevu(Long randevuId, String reason) {
        Optional<Randevu> randevuOpt = randevuRepository.findById(randevuId);
        if (randevuOpt.isPresent()) {
            Randevu randevu = randevuOpt.get();
            
            if (randevu.getCancelled()) {
                return false; // Zaten iptal edilmiş
            }
            
            // Sadece gelecek randevular iptal edilebilir
            if (randevu.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
                return false;
            }
            
            randevu.setCancelled(true);
            randevu.setCancellationReason(reason);
            randevu.setCancelledAt(LocalDateTime.now());
            
            randevuRepository.save(randevu);
            return true;
        }
        return false;
    }
    
    /**
     * Randevuya doktor notu ekler
     * @param randevuId Randevu ID
     * @param notes Doktor notları
     * @return boolean işlem başarılı mı
     */
    public boolean addDoctorNotes(Long randevuId, String notes) {
        Optional<Randevu> randevuOpt = randevuRepository.findById(randevuId);
        if (randevuOpt.isPresent()) {
            Randevu randevu = randevuOpt.get();
            randevu.setDoctorNotes(notes);
            randevuRepository.save(randevu);
            return true;
        }
        return false;
    }
    
    /**
     * Randevu çakışması var mı kontrol eder
     * @param doktor Doktor objesi
     * @param appointmentDateTime Randevu tarihi ve saati
     * @return boolean çakışma var mı
     */
    public boolean hasConflictingAppointment(Doktor doktor, LocalDateTime appointmentDateTime) {
        LocalDateTime endTime = appointmentDateTime.plusMinutes(doktor.getAppointmentDuration());
        List<Randevu> conflicts = randevuRepository.findConflictingAppointments(
                doktor, appointmentDateTime, endTime, null);
        return !conflicts.isEmpty();
    }
    
    /**
     * Doktor belirli saatte müsait mi kontrol eder
     * @param doktor Doktor objesi
     * @param appointmentDateTime Randevu tarihi ve saati
     * @return boolean müsait mi
     */
    public boolean isDoctorAvailable(Doktor doktor, LocalDateTime appointmentDateTime) {
        LocalTime appointmentTime = appointmentDateTime.toLocalTime();
        
        // Çalışma saatleri kontrolü
        if (doktor.getWorkStartTime() != null && doktor.getWorkEndTime() != null) {
            return !appointmentTime.isBefore(doktor.getWorkStartTime()) && 
                   !appointmentTime.isAfter(doktor.getWorkEndTime().minusMinutes(doktor.getAppointmentDuration()));
        }
        
        return true; // Çalışma saatleri belirtilmemişse müsait kabul et
    }
    
    /**
     * Doktorun belirli tarihte müsait saatlerini getirir
     * @param doktor Doktor objesi
     * @param date Tarih
     * @return List<LocalTime> müsait saatler
     */
    public List<LocalTime> getAvailableSlots(Doktor doktor, LocalDate date) {
        List<LocalTime> availableSlots = new java.util.ArrayList<>();
        
        if (doktor.getWorkStartTime() == null || doktor.getWorkEndTime() == null) {
            return availableSlots; // Çalışma saatleri belirlenmemişse boş liste döndür
        }
        
        LocalTime currentSlot = doktor.getWorkStartTime();
        int duration = doktor.getAppointmentDuration();
        
        while (currentSlot.isBefore(doktor.getWorkEndTime().minusMinutes(duration))) {
            LocalDateTime slotDateTime = date.atTime(currentSlot);
            
            // Geçmiş saatleri dahil etme
            if (slotDateTime.isAfter(LocalDateTime.now()) && !hasConflictingAppointment(doktor, slotDateTime)) {
                availableSlots.add(currentSlot);
            }
            
            currentSlot = currentSlot.plusMinutes(duration);
        }
        
        return availableSlots;
    }
    
    /**
     * Tüm randevuları getirir
     * @return List<Randevu> tüm randevular
     */
    public List<Randevu> findAll() {
        return randevuRepository.findAll();
    }
    
    /**
     * Tarih aralığındaki randevuları getirir
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return List<Randevu> belirtilen tarih aralığındaki randevular
     */
    public List<Randevu> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return randevuRepository.findByAppointmentDateTimeBetween(startDate, endDate);
    }
    
    /**
     * Son randevuları getirir
     * @param limit Sonuç sayısı
     * @return List<Randevu> en son randevular
     */
    public List<Randevu> getRecentAppointments(int limit) {
        return randevuRepository.findRecentAppointments(limit);
    }
    
    /**
     * Randevu istatistiklerini hesaplar
     * @return RandevuStats randevu istatistik objesi
     */
    public RandevuStats getRandevuStatistics() {
        long toplamSayi = randevuRepository.count();
        long bekleyenSayi = randevuRepository.countByStatus(RandevuDurumu.BEKLEMEDE);
        long onaylananSayi = randevuRepository.countByStatus(RandevuDurumu.ONAYLANDI);
        long reddedilenSayi = randevuRepository.countByStatus(RandevuDurumu.REDDEDILDI);
        
        return new RandevuStats(toplamSayi, bekleyenSayi, onaylananSayi, reddedilenSayi);
    }
    
    /**
     * Randevu istatistikleri için inner class
     */
    public static class RandevuStats {
        private long toplamSayi;
        private long bekleyenSayi;
        private long onaylananSayi;
        private long reddedilenSayi;
        
        public RandevuStats(long toplamSayi, long bekleyenSayi, long onaylananSayi, long reddedilenSayi) {
            this.toplamSayi = toplamSayi;
            this.bekleyenSayi = bekleyenSayi;
            this.onaylananSayi = onaylananSayi;
            this.reddedilenSayi = reddedilenSayi;
        }
        
        // Getters
        public long getToplamSayi() { return toplamSayi; }
        public long getBekleyenSayi() { return bekleyenSayi; }
        public long getOnaylananSayi() { return onaylananSayi; }
        public long getReddedilenSayi() { return reddedilenSayi; }
        public long getTamamlananSayi() { return onaylananSayi; }
    }
} 