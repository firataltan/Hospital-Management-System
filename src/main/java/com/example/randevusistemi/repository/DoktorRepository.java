package com.example.randevusistemi.repository;

import com.example.randevusistemi.entity.Doktor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Doktor entity için repository interface
 * Doktor özel işlemlerini sağlar
 */
@Repository
public interface DoktorRepository extends JpaRepository<Doktor, Long> {
    
    /**
     * Diploma numarasına göre doktor bulur
     * Unique diploma numarası kontrolü için
     * @param diplomaNo Diploma numarası
     * @return Optional<Doktor> doktor varsa Doktor, yoksa empty
     */
    Optional<Doktor> findByDiplomaNo(String diplomaNo);
    
    /**
     * Diploma numarası var mı kontrol eder
     * Kayıt sırasında unique kontrolü için
     * @param diplomaNo Diploma numarası
     * @return boolean true ise diploma numarası zaten mevcut
     */
    boolean existsByDiplomaNo(String diplomaNo);
    
    /**
     * Kullanıcı adına göre doktor bulur
     * Login işlemi için doktor özel verilerini almak
     * @param username Kullanıcı adı
     * @return Optional<Doktor> doktor varsa Doktor, yoksa empty
     */
    Optional<Doktor> findByUsername(String username);
    
    /**
     * Email adresine göre doktor bulur
     * @param email Email adresi
     * @return Optional<Doktor> doktor varsa Doktor, yoksa empty
     */
    Optional<Doktor> findByEmail(String email);
    
    /**
     * Uzmanlık alanına göre doktorları bulur
     * Branş bazlı doktor listeleme için
     * @param specialization Uzmanlık alanı
     * @return List<Doktor> belirtilen uzmanlık alanındaki doktorlar
     */
    List<Doktor> findBySpecializationContainingIgnoreCase(String specialization);
    
    /**
     * Hastaneye göre doktorları bulur
     * @param hospital Hastane adı
     * @return List<Doktor> belirtilen hastanede çalışan doktorlar
     */
    List<Doktor> findByHospitalContainingIgnoreCase(String hospital);
    
    /**
     * Aktif doktorları bulur
     * @param active Aktiflik durumu
     * @return List<Doktor> aktif/pasif doktorlar
     */
    List<Doktor> findByActive(Boolean active);
    
    /**
     * Randevu kabul eden doktorları bulur
     * Randevu alma sayfası için
     * @param acceptingAppointments Randevu kabul durumu
     * @return List<Doktor> randevu kabul eden doktorlar
     */
    List<Doktor> findByAcceptingAppointments(Boolean acceptingAppointments);
    
    /**
     * Aktif ve randevu kabul eden doktorları bulur
     * @param active Aktiflik durumu
     * @param acceptingAppointments Randevu kabul durumu
     * @return List<Doktor> kriterlere uyan doktorlar
     */
    List<Doktor> findByActiveAndAcceptingAppointments(Boolean active, Boolean acceptingAppointments);
    
    /**
     * Deneyim yılına göre doktorları bulur
     * @param minYears Minimum deneyim yılı
     * @return List<Doktor> belirtilen deneyimden fazla olan doktorlar
     */
    List<Doktor> findByExperienceYearsGreaterThanEqual(Integer minYears);
    
    /**
     * Randevu ücret aralığına göre doktorları bulur
     * @param minFee Minimum ücret
     * @param maxFee Maksimum ücret
     * @return List<Doktor> belirtilen ücret aralığındaki doktorlar
     */
    List<Doktor> findByConsultationFeeBetween(Double minFee, Double maxFee);
    
    /**
     * Çalışma saati aralığına göre doktorları bulur
     * @param startTime Başlangıç saati
     * @param endTime Bitiş saati
     * @return List<Doktor> belirtilen saatlerde çalışan doktorlar
     */
    @Query("SELECT d FROM Doktor d WHERE d.workStartTime <= :startTime AND d.workEndTime >= :endTime")
    List<Doktor> findByWorkingHours(@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
    
    /**
     * Şehre göre doktorları bulur (klinik adresi veya hastane bilgisinden)
     * @param city Şehir adı
     * @return List<Doktor> belirtilen şehirde çalışan doktorlar
     */
    @Query("SELECT d FROM Doktor d WHERE " +
           "LOWER(d.clinicAddress) LIKE LOWER(CONCAT('%', :city, '%')) OR " +
           "LOWER(d.hospital) LIKE LOWER(CONCAT('%', :city, '%'))")
    List<Doktor> findByCity(@Param("city") String city);
    
    /**
     * En çok randevu alan doktorları bulur
     * Top doktor listesi için
     * @return List<Doktor> en çok randevu alan doktorlar
     */
    @Query("SELECT d FROM Doktor d " +
           "LEFT JOIN d.randevular r " +
           "GROUP BY d.id " +
           "ORDER BY COUNT(r.id) DESC")
    List<Doktor> findTopDoctorsByAppointmentCount();
    
    /**
     * Uzmanlık alanına göre benzersiz listesi
     * Filtreleme seçenekleri için
     * @return List<String> tüm uzmanlık alanları
     */
    @Query("SELECT DISTINCT d.specialization FROM Doktor d WHERE d.specialization IS NOT NULL ORDER BY d.specialization")
    List<String> findDistinctSpecializations();
    
    /**
     * Hastane listesi
     * Filtreleme seçenekleri için
     * @return List<String> tüm hastaneler
     */
    @Query("SELECT DISTINCT d.hospital FROM Doktor d WHERE d.hospital IS NOT NULL ORDER BY d.hospital")
    List<String> findDistinctHospitals();
    
    /**
     * Randevu süresine göre doktorları bulur
     * @param duration Randevu süresi (dakika)
     * @return List<Doktor> belirtilen randevu süresine sahip doktorlar
     */
    List<Doktor> findByAppointmentDuration(Integer duration);
} 