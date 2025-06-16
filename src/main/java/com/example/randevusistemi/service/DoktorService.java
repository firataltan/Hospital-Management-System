package com.example.randevusistemi.service;

import com.example.randevusistemi.entity.Doktor;
import com.example.randevusistemi.entity.Randevu;
import com.example.randevusistemi.entity.User;
import com.example.randevusistemi.repository.DoktorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Doktor yönetimi için service sınıfı
 * Doktor özel işlemlerini yönetir
 */
@Service
@Transactional
public class DoktorService {
    
    @Autowired
    private DoktorRepository doktorRepository;
    
    @Autowired
    private RandevuService randevuService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * ID'ye göre doktor bulur
     * @param id Doktor ID
     * @return Optional<Doktor> doktor varsa Doktor objesi
     */
    public Optional<Doktor> findById(Long id) {
        return doktorRepository.findById(id);
    }
    
    /**
     * Kullanıcı adına göre doktor bulur
     * @param username Kullanıcı adı
     * @return Optional<Doktor> doktor varsa Doktor objesi
     */
    public Optional<Doktor> findByUsername(String username) {
        return doktorRepository.findByUsername(username);
    }
    
    /**
     * Diploma numarasına göre doktor bulur
     * @param diplomaNo Diploma numarası
     * @return Optional<Doktor> doktor varsa Doktor objesi
     */
    public Optional<Doktor> findByDiplomaNo(String diplomaNo) {
        return doktorRepository.findByDiplomaNo(diplomaNo);
    }
    
    /**
     * Email adresine göre doktor bulur
     * @param email Email adresi
     * @return Optional<Doktor> doktor varsa Doktor objesi
     */
    public Optional<Doktor> findByEmail(String email) {
        return doktorRepository.findByEmail(email);
    }
    
    /**
     * Diploma numarası zaten kullanılıyor mu kontrol eder
     * @param diplomaNo Diploma numarası
     * @return boolean true ise zaten mevcut
     */
    public boolean existsByDiplomaNo(String diplomaNo) {
        return doktorRepository.existsByDiplomaNo(diplomaNo);
    }
    
    /**
     * Doktor kaydeder
     * @param doktor Kaydedilecek doktor
     * @return Doktor kaydedilen doktor
     */
    public Doktor save(Doktor doktor) {
        return doktorRepository.save(doktor);
    }
    
    /**
     * Doktor profilini günceller
     * @param doktor Güncellenecek doktor
     * @return Doktor güncellenmiş doktor
     */
    public Doktor updateProfile(Doktor doktor) {
        return doktorRepository.save(doktor);
    }
    
    /**
     * Tüm doktorları listeler
     * @return List<Doktor> tüm doktorlar
     */
    public List<Doktor> findAll() {
        return doktorRepository.findAll();
    }
    
    /**
     * Aktif doktorları listeler
     * @param active Aktiflik durumu
     * @return List<Doktor> aktif/pasif doktorlar
     */
    public List<Doktor> findByActive(boolean active) {
        return doktorRepository.findByActive(active);
    }
    
    /**
     * Randevu kabul eden doktorları listeler
     * @return List<Doktor> randevu kabul eden doktorlar
     */
    public List<Doktor> findAcceptingAppointments() {
        return doktorRepository.findByAcceptingAppointments(true);
    }
    
    /**
     * Aktif ve randevu kabul eden doktorları listeler
     * @return List<Doktor> müsait doktorlar
     */
    public List<Doktor> findAvailableDoctors() {
        return doktorRepository.findByActiveAndAcceptingAppointments(true, true);
    }
    
    /**
     * Uzmanlık alanına göre doktor arar
     * @param specialization Uzmanlık alanı
     * @return List<Doktor> belirtilen uzmanlık alanındaki doktorlar
     */
    public List<Doktor> findBySpecialization(String specialization) {
        return doktorRepository.findBySpecializationContainingIgnoreCase(specialization);
    }
    
    /**
     * Hastaneye göre doktor arar
     * @param hospital Hastane adı
     * @return List<Doktor> belirtilen hastanedeki doktorlar
     */
    public List<Doktor> findByHospital(String hospital) {
        return doktorRepository.findByHospitalContainingIgnoreCase(hospital);
    }
    
    /**
     * Minimum deneyim yılına göre doktor arar
     * @param minYears Minimum deneyim yılı
     * @return List<Doktor> belirtilen deneyimden fazla olan doktorlar
     */
    public List<Doktor> findByMinimumExperience(int minYears) {
        return doktorRepository.findByExperienceYearsGreaterThanEqual(minYears);
    }
    
    /**
     * Ücret aralığına göre doktor arar
     * @param minFee Minimum ücret
     * @param maxFee Maksimum ücret
     * @return List<Doktor> belirtilen ücret aralığındaki doktorlar
     */
    public List<Doktor> findByFeeRange(double minFee, double maxFee) {
        return doktorRepository.findByConsultationFeeBetween(minFee, maxFee);
    }
    
    /**
     * Şehre göre doktor arar
     * @param city Şehir adı
     * @return List<Doktor> belirtilen şehirdeki doktorlar
     */
    public List<Doktor> findByCity(String city) {
        return doktorRepository.findByCity(city);
    }
    
    /**
     * Çalışma saatlerine göre doktor arar
     * @param startTime Başlangıç saati
     * @param endTime Bitiş saati
     * @return List<Doktor> belirtilen saatlerde çalışan doktorlar
     */
    public List<Doktor> findByWorkingHours(LocalTime startTime, LocalTime endTime) {
        return doktorRepository.findByWorkingHours(startTime, endTime);
    }
    
    /**
     * Doktorun randevularını getirir
     * @param doktor Doktor objesi
     * @return List<Randevu> doktorun randevuları
     */
    public List<Randevu> getDoktorRandevulari(Doktor doktor) {
        return randevuService.findByDoktor(doktor);
    }
    
    /**
     * Doktorun bekleyen randevularını getirir
     * @param doktor Doktor objesi
     * @return List<Randevu> onay bekleyen randevular
     */
    public List<Randevu> getDoktorBekleyenRandevulari(Doktor doktor) {
        return randevuService.findByDoktorBekleyen(doktor);
    }
    
    /**
     * Doktorun bugünkü randevularını getirir
     * @param doktor Doktor objesi
     * @return List<Randevu> bugünkü randevular
     */
    public List<Randevu> getDoktorBugunRandevulari(Doktor doktor) {
        return randevuService.findByDoktorBugun(doktor);
    }
    
    /**
     * Doktorun randevu kabul durumunu değiştirir
     * @param doktorId Doktor ID
     * @param accepting Kabul durumu
     * @return boolean işlem başarılı mı
     */
    public boolean toggleAppointmentAcceptance(Long doktorId, boolean accepting) {
        Optional<Doktor> doktorOpt = doktorRepository.findById(doktorId);
        if (doktorOpt.isPresent()) {
            Doktor doktor = doktorOpt.get();
            doktor.setAcceptingAppointments(accepting);
            doktorRepository.save(doktor);
            return true;
        }
        return false;
    }
    
    /**
     * Doktor çalışma saatlerini günceller
     * @param doktorId Doktor ID
     * @param startTime Başlangıç saati
     * @param endTime Bitiş saati
     * @param duration Randevu süresi (dakika)
     * @return boolean işlem başarılı mı
     */
    public boolean updateWorkingHours(Long doktorId, LocalTime startTime, LocalTime endTime, int duration) {
        Optional<Doktor> doktorOpt = doktorRepository.findById(doktorId);
        if (doktorOpt.isPresent()) {
            Doktor doktor = doktorOpt.get();
            doktor.setWorkStartTime(startTime);
            doktor.setWorkEndTime(endTime);
            doktor.setAppointmentDuration(duration);
            doktorRepository.save(doktor);
            return true;
        }
        return false;
    }
    
    /**
     * Doktoru siler (pasif yapar)
     * @param doktorId Doktor ID
     * @return boolean silme işlemi başarılı mı
     */
    public boolean deleteDoktor(Long doktorId) {
        try {
            Optional<Doktor> doktorOpt = doktorRepository.findById(doktorId);
            if (doktorOpt.isPresent()) {
                Doktor doktor = doktorOpt.get();
                
                // Önce gelecek randevuları iptal et
                List<Randevu> gelecekRandevular = randevuService.findByDoktorGelecek(doktor);
                for (Randevu randevu : gelecekRandevular) {
                    randevuService.cancelRandevu(randevu.getId(), "Doktor hesabı silindi");
                }
                
                // Doktoru pasif yap (tam silme yerine)
                doktor.setActive(false);
                doktor.setAcceptingAppointments(false);
                doktorRepository.save(doktor);
                
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Tüm uzmanlık alanlarını getirir
     * @return List<String> benzersiz uzmanlık alanları
     */
    public List<String> getAllSpecializations() {
        return doktorRepository.findDistinctSpecializations();
    }
    
    /**
     * Tüm hastaneleri getirir
     * @return List<String> benzersiz hastaneler
     */
    public List<String> getAllHospitals() {
        return doktorRepository.findDistinctHospitals();
    }
    
    /**
     * En çok randevu alan doktorları getirir
     * @return List<Doktor> en çok randevu alan doktorlar
     */
    public List<Doktor> getTopDoctorsByAppointmentCount() {
        return doktorRepository.findTopDoctorsByAppointmentCount();
    }
    
    /**
     * Doktor sayısını döndürür
     * @return long toplam doktor sayısı
     */
    public long getTotalDoktorCount() {
        return doktorRepository.count();
    }
    
    /**
     * Aktif doktor sayısını döndürür
     * @return long aktif doktor sayısı
     */
    public long getActiveDoktorCount() {
        return doktorRepository.findByActive(true).size();
    }
    
    /**
     * Randevu kabul eden doktor sayısını döndürür
     * @return long randevu kabul eden doktor sayısı
     */
    public long getAcceptingAppointmentsCount() {
        return doktorRepository.findByAcceptingAppointments(true).size();
    }
    
    /**
     * Doktor istatistiklerini hesaplar
     * @return DoktorStats doktor istatistik objesi
     */
    public DoktorStats getDoktorStatistics() {
        List<Doktor> tumDoktorlar = doktorRepository.findAll();
        
        long toplamSayi = tumDoktorlar.size();
        long aktifSayi = tumDoktorlar.stream().filter(User::getActive).count();
        long randevuKabulEdenSayi = tumDoktorlar.stream().filter(Doktor::getAcceptingAppointments).count();
        long uzmanlıkAlaniSayisi = getAllSpecializations().size();
        
        return new DoktorStats(toplamSayi, aktifSayi, randevuKabulEdenSayi, uzmanlıkAlaniSayisi);
    }
    
    /**
     * Doktor istatistikleri için inner class
     */
    public static class DoktorStats {
        private long toplamSayi;
        private long aktifSayi;
        private long randevuKabulEdenSayi;
        private long uzmanlikAlaniSayisi;
        
        public DoktorStats(long toplamSayi, long aktifSayi, long randevuKabulEdenSayi, long uzmanlikAlaniSayisi) {
            this.toplamSayi = toplamSayi;
            this.aktifSayi = aktifSayi;
            this.randevuKabulEdenSayi = randevuKabulEdenSayi;
            this.uzmanlikAlaniSayisi = uzmanlikAlaniSayisi;
        }
        
        // Getters
        public long getToplamSayi() { return toplamSayi; }
        public long getAktifSayi() { return aktifSayi; }
        public long getRandevuKabulEdenSayi() { return randevuKabulEdenSayi; }
        public long getUzmanlikAlaniSayisi() { return uzmanlikAlaniSayisi; }
        public long getPasifSayi() { return toplamSayi - aktifSayi; }
    }
    
    public Doktor registerDoktor(Doktor doktor) {
        doktor.setPassword(passwordEncoder.encode(doktor.getPassword()));
        doktor.setRole(com.example.randevusistemi.enums.Role.DOKTOR);
        doktor.setActive(true);
        return doktorRepository.save(doktor);
    }
} 