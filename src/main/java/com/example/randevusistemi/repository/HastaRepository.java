package com.example.randevusistemi.repository;

import com.example.randevusistemi.entity.Hasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Hasta entity için repository interface
 * Hasta özel işlemlerini sağlar
 */
@Repository
public interface HastaRepository extends JpaRepository<Hasta, Long> {
    
    /**
     * T.C. kimlik numarasına göre hasta bulur
     * Unique T.C. kontrolü ve hasta arama için
     * @param tcNo T.C. kimlik numarası
     * @return Optional<Hasta> hasta varsa Hasta, yoksa empty
     */
    Optional<Hasta> findByTcNo(String tcNo);
    
    /**
     * T.C. kimlik numarası var mı kontrol eder
     * Kayıt sırasında unique kontrolü için
     * @param tcNo T.C. kimlik numarası
     * @return boolean true ise T.C. zaten mevcut
     */
    boolean existsByTcNo(String tcNo);
    
    /**
     * Kullanıcı adına göre hasta bulur
     * Login işlemi için hasta özel verilerini almak
     * @param username Kullanıcı adı
     * @return Optional<Hasta> hasta varsa Hasta, yoksa empty
     */
    Optional<Hasta> findByUsername(String username);
    
    /**
     * Email adresine göre hasta bulur
     * @param email Email adresi
     * @return Optional<Hasta> hasta varsa Hasta, yoksa empty
     */
    Optional<Hasta> findByEmail(String email);
    
    /**
     * Aktif hastaları bulur
     * @param active Aktiflik durumu
     * @return List<Hasta> aktif/pasif hastalar
     */
    List<Hasta> findByActive(Boolean active);
    
    /**
     * Cinsiyete göre hastaları bulur
     * İstatistik raporları için
     * @param gender Cinsiyet
     * @return List<Hasta> belirtilen cinsiyetteki hastalar
     */
    List<Hasta> findByGender(String gender);
    
    /**
     * Doğum tarihi aralığına göre hastaları bulur
     * Yaş grubu analizleri için
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return List<Hasta> belirtilen tarih aralığında doğan hastalar
     */
    List<Hasta> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Belirli bir yaştan büyük hastaları bulur
     * @param birthDate Doğum tarihi (yaş hesabı için)
     * @return List<Hasta> belirtilen yaştan büyük hastalar
     */
    List<Hasta> findByBirthDateBefore(LocalDate birthDate);
    
    /**
     * Belirli bir yaştan küçük hastaları bulur
     * @param birthDate Doğum tarihi (yaş hesabı için)
     * @return List<Hasta> belirtilen yaştan küçük hastalar
     */
    List<Hasta> findByBirthDateAfter(LocalDate birthDate);
    
    /**
     * Telefon numarasına göre hasta arar
     * @param phone Telefon numarası
     * @return List<Hasta> telefon numarası eşleşen hastalar
     */
    List<Hasta> findByPhoneContaining(String phone);
    
    /**
     * Şehre göre hastaları bulur (adres alanından)
     * @param city Şehir adı
     * @return List<Hasta> belirtilen şehirde yaşayan hastalar
     */
    @Query("SELECT h FROM Hasta h WHERE LOWER(h.address) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<Hasta> findByCity(@Param("city") String city);
    
    /**
     * En çok randevu alan hastaları bulur
     * Top hasta listesi için
     * @param limit Sonuç sayısı limiti
     * @return List<Hasta> en çok randevu alan hastalar
     */
    @Query("SELECT h FROM Hasta h " +
           "LEFT JOIN h.randevular r " +
           "GROUP BY h.id " +
           "ORDER BY COUNT(r.id) DESC")
    List<Hasta> findTopPatientsByAppointmentCount(@Param("limit") int limit);
    
    /**
     * Son kayıt olan hastaları bulur
     * @param limit Sonuç sayısı limiti
     * @return List<Hasta> son kayıt olan hastalar
     */
    @Query("SELECT h FROM Hasta h ORDER BY h.createdAt DESC")
    List<Hasta> findRecentPatients(@Param("limit") int limit);
} 