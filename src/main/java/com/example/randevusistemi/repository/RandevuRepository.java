package com.example.randevusistemi.repository;

import com.example.randevusistemi.entity.Randevu;
import com.example.randevusistemi.entity.Hasta;
import com.example.randevusistemi.entity.Doktor;
import com.example.randevusistemi.enums.RandevuDurumu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Randevu entity için repository interface
 * Randevu yönetimi için gerekli tüm sorguları sağlar
 */
@Repository
public interface RandevuRepository extends JpaRepository<Randevu, Long> {
    
    /**
     * Hastaya ait randevuları bulur (tarih sıralı)
     * @param hasta Hasta entity
     * @return List<Randevu> hastanın randevuları (yeniden eskiye)
     */
    List<Randevu> findByHastaOrderByAppointmentDateTimeDesc(Hasta hasta);
    
    /**
     * Doktora ait randevuları bulur (tarih sıralı)
     * @param doktor Doktor entity
     * @return List<Randevu> doktorun randevuları (yeniden eskiye)
     */
    List<Randevu> findByDoktorOrderByAppointmentDateTimeAsc(Doktor doktor);
    
    /**
     * Duruma göre randevuları bulur
     * @param status Randevu durumu
     * @return List<Randevu> belirtilen durumdaki randevular
     */
    List<Randevu> findByStatus(RandevuDurumu status);
    
    /**
     * Doktor ve duruma göre randevuları bulur
     * @param doktor Doktor entity
     * @param status Randevu durumu
     * @return List<Randevu> kriterlere uyan randevular
     */
    List<Randevu> findByDoktorAndStatus(Doktor doktor, RandevuDurumu status);
    
    /**
     * Hasta ve duruma göre randevuları bulur
     * @param hasta Hasta entity
     * @param status Randevu durumu
     * @return List<Randevu> kriterlere uyan randevular
     */
    List<Randevu> findByHastaAndStatus(Hasta hasta, RandevuDurumu status);
    
    /**
     * Tarih aralığındaki randevuları bulur
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return List<Randevu> belirtilen tarih aralığındaki randevular
     */
    List<Randevu> findByAppointmentDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Doktor ve tarih aralığına göre randevuları bulur
     * Çakışma kontrolü için kullanılır
     * @param doktor Doktor entity
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return List<Randevu> kriterlere uyan randevular
     */
    List<Randevu> findByDoktorAndAppointmentDateTimeBetween(
            Doktor doktor, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Belirli tarih ve saatte doktorun randevusu var mı kontrol eder
     * Çakışma kontrolü için
     * @param doktor Doktor entity
     * @param appointmentDateTime Randevu tarihi ve saati
     * @return boolean true ise çakışma var
     */
    boolean existsByDoktorAndAppointmentDateTime(Doktor doktor, LocalDateTime appointmentDateTime);
    
    /**
     * İptal edilmemiş randevuları bulur
     * @param cancelled İptal durumu
     * @return List<Randevu> iptal edilmemiş randevular
     */
    List<Randevu> findByCancelled(Boolean cancelled);
    
    /**
     * Doktorun bugünkü randevularını bulur
     * @param doktor Doktor entity
     * @param startOfDay Günün başlangıcı
     * @param endOfDay Günün sonu
     * @return List<Randevu> bugünkü randevular
     */
    @Query("SELECT r FROM Randevu r WHERE r.doktor = :doktor AND " +
           "r.appointmentDateTime >= :startOfDay AND r.appointmentDateTime <= :endOfDay AND " +
           "r.cancelled = false ORDER BY r.appointmentDateTime ASC")
    List<Randevu> findTodayAppointments(@Param("doktor") Doktor doktor, 
                                       @Param("startOfDay") LocalDateTime startOfDay,
                                       @Param("endOfDay") LocalDateTime endOfDay);
    
    /**
     * Gelecek randevuları bulur
     * @param currentDateTime Şu anki zaman
     * @return List<Randevu> gelecek randevular
     */
    List<Randevu> findByAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(LocalDateTime currentDateTime);
    
    /**
     * Geçmiş randevuları bulur
     * @param currentDateTime Şu anki zaman
     * @return List<Randevu> geçmiş randevular
     */
    List<Randevu> findByAppointmentDateTimeBeforeOrderByAppointmentDateTimeDesc(LocalDateTime currentDateTime);
    
    /**
     * Hastanın gelecek randevularını bulur
     * @param hasta Hasta entity
     * @param currentDateTime Şu anki zaman
     * @return List<Randevu> hastanın gelecek randevuları
     */
    List<Randevu> findByHastaAndAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(
            Hasta hasta, LocalDateTime currentDateTime);
    
    /**
     * Hastanın geçmiş randevularını bulur
     * @param hasta Hasta entity
     * @param currentDateTime Şu anki zaman
     * @return List<Randevu> hastanın geçmiş randevuları
     */
    List<Randevu> findByHastaAndAppointmentDateTimeBeforeOrderByAppointmentDateTimeDesc(
            Hasta hasta, LocalDateTime currentDateTime);
    
    /**
     * Doktorun bekleyen randevularını bulur
     * @param doktor Doktor entity
     * @return List<Randevu> onay bekleyen randevular
     */
    @Query("SELECT r FROM Randevu r WHERE r.doktor = :doktor AND r.status = 'BEKLEMEDE' AND " +
           "r.cancelled = false ORDER BY r.appointmentDateTime ASC")
    List<Randevu> findPendingAppointments(@Param("doktor") Doktor doktor);
    
    /**
     * Randevu çakışması kontrolü yapar
     * Aynı doktor için belirli zaman aralığında randevu var mı kontrol eder
     * @param doktor Doktor entity
     * @param startTime Başlangıç zamanı
     * @param endTime Bitiş zamanı
     * @param excludeId Hariç tutulacak randevu ID (güncelleme için)
     * @return List<Randevu> çakışan randevular
     */
    @Query("SELECT r FROM Randevu r WHERE r.doktor = :doktor AND " +
           "r.cancelled = false AND " +
           "(:excludeId IS NULL OR r.id != :excludeId) AND " +
           "((r.appointmentDateTime < :endTime) AND (r.appointmentDateTime >= :startTime))")
    List<Randevu> findConflictingAppointments(@Param("doktor") Doktor doktor,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime,
                                             @Param("excludeId") Long excludeId);
    
    /**
     * En son randevuları bulur (dashboard için)
     * @param limit Sonuç sayısı limiti
     * @return List<Randevu> en son randevular
     */
    @Query("SELECT r FROM Randevu r ORDER BY r.createdAt DESC")
    List<Randevu> findRecentAppointments(@Param("limit") int limit);
    
    /**
     * Randevu istatistikleri için duruma göre sayım
     * @param status Randevu durumu
     * @return Long randevu sayısı
     */
    Long countByStatus(RandevuDurumu status);
    
    /**
     * Doktor için randevu sayısı
     * @param doktor Doktor entity
     * @return Long doktorun toplam randevu sayısı
     */
    Long countByDoktor(Doktor doktor);
    
    /**
     * Hasta için randevu sayısı
     * @param hasta Hasta entity
     * @return Long hastanın toplam randevu sayısı
     */
    Long countByHasta(Hasta hasta);
} 