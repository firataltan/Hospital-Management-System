package com.example.randevusistemi.service;

import com.example.randevusistemi.entity.Hasta;
import com.example.randevusistemi.entity.Randevu;
import com.example.randevusistemi.repository.HastaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Hasta yönetimi için service sınıfı
 * Hasta özel işlemlerini yönetir
 */
@Service
@Transactional
public class HastaService {
    
    @Autowired
    private HastaRepository hastaRepository;
    
    @Autowired
    private RandevuService randevuService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * ID'ye göre hasta bulur
     * @param id Hasta ID
     * @return Optional<Hasta> hasta varsa Hasta objesi
     */
    public Optional<Hasta> findById(Long id) {
        return hastaRepository.findById(id);
    }
    
    /**
     * Kullanıcı adına göre hasta bulur
     * @param username Kullanıcı adı
     * @return Optional<Hasta> hasta varsa Hasta objesi
     */
    public Optional<Hasta> findByUsername(String username) {
        return hastaRepository.findByUsername(username);
    }
    
    /**
     * T.C. kimlik numarasına göre hasta bulur
     * @param tcNo T.C. kimlik numarası
     * @return Optional<Hasta> hasta varsa Hasta objesi
     */
    public Optional<Hasta> findByTcNo(String tcNo) {
        return hastaRepository.findByTcNo(tcNo);
    }
    
    /**
     * Email adresine göre hasta bulur
     * @param email Email adresi
     * @return Optional<Hasta> hasta varsa Hasta objesi
     */
    public Optional<Hasta> findByEmail(String email) {
        return hastaRepository.findByEmail(email);
    }
    
    /**
     * T.C. kimlik numarası zaten kullanılıyor mu kontrol eder
     * @param tcNo T.C. kimlik numarası
     * @return boolean true ise zaten mevcut
     */
    public boolean existsByTcNo(String tcNo) {
        return hastaRepository.existsByTcNo(tcNo);
    }
    
    /**
     * Hasta kaydeder
     * @param hasta Kaydedilecek hasta
     * @return Hasta kaydedilen hasta
     */
    public Hasta save(Hasta hasta) {
        return hastaRepository.save(hasta);
    }
    
    /**
     * Hasta profilini günceller
     * @param hasta Güncellenecek hasta
     * @return Hasta güncellenmiş hasta
     */
    public Hasta updateProfile(Hasta hasta) {
        return hastaRepository.save(hasta);
    }
    
    /**
     * Tüm hastaları listeler
     * @return List<Hasta> tüm hastalar
     */
    public List<Hasta> findAll() {
        return hastaRepository.findAll();
    }
    
    /**
     * Aktif hastaları listeler
     * @param active Aktiflik durumu
     * @return List<Hasta> aktif/pasif hastalar
     */
    public List<Hasta> findByActive(boolean active) {
        return hastaRepository.findByActive(active);
    }
    
    /**
     * Cinsiyete göre hastaları listeler
     * @param gender Cinsiyet
     * @return List<Hasta> belirtilen cinsiyetteki hastalar
     */
    public List<Hasta> findByGender(String gender) {
        return hastaRepository.findByGender(gender);
    }
    
    /**
     * Yaş aralığına göre hastaları bulur
     * @param minAge Minimum yaş
     * @param maxAge Maksimum yaş
     * @return List<Hasta> belirtilen yaş aralığındaki hastalar
     */
    public List<Hasta> findByAgeRange(int minAge, int maxAge) {
        LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);
        LocalDate minBirthDate = LocalDate.now().minusYears(maxAge + 1);
        return hastaRepository.findByBirthDateBetween(minBirthDate, maxBirthDate);
    }
    
    /**
     * Belirli yaştan büyük hastaları bulur
     * @param age Yaş sınırı
     * @return List<Hasta> belirtilen yaştan büyük hastalar
     */
    public List<Hasta> findOlderThan(int age) {
        LocalDate birthDate = LocalDate.now().minusYears(age + 1);
        return hastaRepository.findByBirthDateBefore(birthDate);
    }
    
    /**
     * Belirli yaştan küçük hastaları bulur
     * @param age Yaş sınırı
     * @return List<Hasta> belirtilen yaştan küçük hastalar
     */
    public List<Hasta> findYoungerThan(int age) {
        LocalDate birthDate = LocalDate.now().minusYears(age);
        return hastaRepository.findByBirthDateAfter(birthDate);
    }
    
    /**
     * Telefon numarasına göre hasta arar
     * @param phone Telefon numarası
     * @return List<Hasta> telefon numarası eşleşen hastalar
     */
    public List<Hasta> findByPhone(String phone) {
        return hastaRepository.findByPhoneContaining(phone);
    }
    
    /**
     * Şehre göre hastaları bulur
     * @param city Şehir adı
     * @return List<Hasta> belirtilen şehirdeki hastalar
     */
    public List<Hasta> findByCity(String city) {
        return hastaRepository.findByCity(city);
    }
    
    /**
     * Hastanın randevularını getirir
     * @param hasta Hasta objesi
     * @return List<Randevu> hastanın randevuları
     */
    public List<Randevu> getHastaRandevulari(Hasta hasta) {
        return randevuService.findByHasta(hasta);
    }
    
    /**
     * Hastanın gelecek randevularını getirir
     * @param hasta Hasta objesi
     * @return List<Randevu> hastanın gelecek randevuları
     */
    public List<Randevu> getHastaGelecekRandevulari(Hasta hasta) {
        return randevuService.findByHastaGelecek(hasta);
    }
    
    /**
     * Hastanın geçmiş randevularını getirir
     * @param hasta Hasta objesi
     * @return List<Randevu> hastanın geçmiş randevuları
     */
    public List<Randevu> getHastaGecmisRandevulari(Hasta hasta) {
        return randevuService.findByHastaGecmis(hasta);
    }
    
    /**
     * Hastayı siler
     * @param hastaId Hasta ID
     * @return boolean silme işlemi başarılı mı
     */
    public boolean deleteHasta(Long hastaId) {
        try {
            Optional<Hasta> hastaOpt = hastaRepository.findById(hastaId);
            if (hastaOpt.isPresent()) {
                Hasta hasta = hastaOpt.get();
                
                // Önce gelecek randevuları iptal et
                List<Randevu> gelecekRandevular = getHastaGelecekRandevulari(hasta);
                for (Randevu randevu : gelecekRandevular) {
                    randevuService.cancelRandevu(randevu.getId(), "Hasta hesabı silindi");
                }
                
                // Hastayı pasif yap (tam silme yerine)
                hasta.setActive(false);
                hastaRepository.save(hasta);
                
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Hasta sayısını döndürür
     * @return long toplam hasta sayısı
     */
    public long getTotalHastaCount() {
        return hastaRepository.count();
    }
    
    /**
     * Aktif hasta sayısını döndürür
     * @return long aktif hasta sayısı
     */
    public long getActiveHastaCount() {
        return hastaRepository.findByActive(true).size();
    }
    
    /**
     * En çok randevu alan hastaları getirir
     * @param limit Sonuç sayısı
     * @return List<Hasta> en çok randevu alan hastalar
     */
    public List<Hasta> getTopPatientsByAppointmentCount(int limit) {
        return hastaRepository.findTopPatientsByAppointmentCount(limit);
    }
    
    /**
     * Son kayıt olan hastaları getirir
     * @param limit Sonuç sayısı
     * @return List<Hasta> son kayıt olan hastalar
     */
    public List<Hasta> getRecentPatients(int limit) {
        return hastaRepository.findRecentPatients(limit);
    }
    
    /**
     * Hasta istatistiklerini hesaplar
     * @return HastaStats hasta istatistik objesi
     */
    public HastaStats getHastaStatistics() {
        List<Hasta> tumHastalar = hastaRepository.findAll();
        
        long toplamSayi = tumHastalar.size();
        long aktifSayi = tumHastalar.stream().filter(Hasta::getActive).count();
        long erkekSayi = tumHastalar.stream().filter(h -> "Erkek".equals(h.getGender())).count();
        long kadinSayi = tumHastalar.stream().filter(h -> "Kadın".equals(h.getGender())).count();
        
        return new HastaStats(toplamSayi, aktifSayi, erkekSayi, kadinSayi);
    }
    
    /**
     * Hasta istatistikleri için inner class
     */
    public static class HastaStats {
        private long toplamSayi;
        private long aktifSayi;
        private long erkekSayi;
        private long kadinSayi;
        
        public HastaStats(long toplamSayi, long aktifSayi, long erkekSayi, long kadinSayi) {
            this.toplamSayi = toplamSayi;
            this.aktifSayi = aktifSayi;
            this.erkekSayi = erkekSayi;
            this.kadinSayi = kadinSayi;
        }
        
        // Getters
        public long getToplamSayi() { return toplamSayi; }
        public long getAktifSayi() { return aktifSayi; }
        public long getErkekSayi() { return erkekSayi; }
        public long getKadinSayi() { return kadinSayi; }
        public long getPasifSayi() { return toplamSayi - aktifSayi; }
    }
    
    public Hasta registerHasta(Hasta hasta) {
        hasta.setPassword(passwordEncoder.encode(hasta.getPassword()));
        hasta.setRole(com.example.randevusistemi.enums.Role.HASTA);
        hasta.setActive(true);
        return hastaRepository.save(hasta);
    }
} 