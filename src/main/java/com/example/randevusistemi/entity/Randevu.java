package com.example.randevusistemi.entity;

import com.example.randevusistemi.enums.RandevuDurumu;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Randevu entity sınıfı
 * Hasta ve doktor arasındaki randevu bilgilerini tutar
 */
@Entity
@Table(name = "randevular")
public class Randevu {
    
    /**
     * Birincil anahtar - otomatik artan ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "randevu_id")
    private Long id;
    
    /**
     * Randevu ile ilişkili hasta - Many-to-One ilişki
     * Bir hasta birden çok randevu alabilir
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hasta_id", nullable = false)
    private Hasta hasta;
    
    /**
     * Randevu ile ilişkili doktor - Many-to-One ilişki
     * Bir doktor birden çok randevu alabilir
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doktor_id", nullable = false)
    private Doktor doktor;
    
    /**
     * Randevu tarihi ve saati
     */
    @Column(name = "appointment_datetime", nullable = false)
    private LocalDateTime appointmentDateTime;
    
    /**
     * Randevu durumu - BEKLEMEDE, ONAYLANDI, REDDEDILDI
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RandevuDurumu status = RandevuDurumu.BEKLEMEDE;
    
    /**
     * Hasta şikayeti/randevu sebebi
     */
    @Column(name = "complaint", length = 1000)
    private String complaint;
    
    /**
     * Doktor notları - muayene sonrası doktor tarafından eklenir
     */
    @Column(name = "doctor_notes", length = 2000)
    private String doctorNotes;
    
    /**
     * Randevu oluşturulma tarihi
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Randevu güncelleme tarihi (onaylama, reddetme, not ekleme)
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Randevu iptal edildi mi?
     */
    @Column(name = "cancelled")
    private Boolean cancelled = false;
    
    /**
     * İptal sebebi
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    /**
     * İptal tarihi
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    /**
     * Entity persist edilmeden önce çalışır
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Entity update edilmeden önce çalışır
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    /**
     * Varsayılan constructor
     */
    public Randevu() {}
    
    /**
     * Parametreli constructor
     * @param hasta Randevu alan hasta
     * @param doktor Randevu veren doktor
     * @param appointmentDateTime Randevu tarihi ve saati
     * @param complaint Hasta şikayeti
     */
    public Randevu(Hasta hasta, Doktor doktor, LocalDateTime appointmentDateTime, String complaint) {
        this.hasta = hasta;
        this.doktor = doktor;
        this.appointmentDateTime = appointmentDateTime;
        this.complaint = complaint;
        this.status = RandevuDurumu.BEKLEMEDE;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Hasta getHasta() { return hasta; }
    public void setHasta(Hasta hasta) { this.hasta = hasta; }
    
    public Doktor getDoktor() { return doktor; }
    public void setDoktor(Doktor doktor) { this.doktor = doktor; }
    
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    
    public RandevuDurumu getStatus() { return status; }
    public void setStatus(RandevuDurumu status) { this.status = status; }
    
    public String getComplaint() { return complaint; }
    public void setComplaint(String complaint) { this.complaint = complaint; }
    
    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Boolean getCancelled() { return cancelled; }
    public void setCancelled(Boolean cancelled) { this.cancelled = cancelled; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    // Utility Methods
    /**
     * Randevu tarihini formatlanmış string olarak döndürür
     * @return String "15 Haziran 2025, 14:30" formatında
     */
    public String getFormattedAppointmentDateTime() {
        if (appointmentDateTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");
            return appointmentDateTime.format(formatter);
        }
        return "";
    }
    
    /**
     * Sadece randevu tarihini döndürür
     * @return String "15 Haziran 2025" formatında
     */
    public String getFormattedAppointmentDate() {
        if (appointmentDateTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            return appointmentDateTime.format(formatter);
        }
        return "";
    }
    
    /**
     * Sadece randevu saatini döndürür
     * @return String "14:30" formatında
     */
    public String getFormattedAppointmentTime() {
        if (appointmentDateTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return appointmentDateTime.format(formatter);
        }
        return "";
    }
    
    /**
     * Randevu geçmiş mi kontrol eder
     * @return boolean true ise randevu geçmiş
     */
    public boolean isPast() {
        return appointmentDateTime != null && appointmentDateTime.isBefore(LocalDateTime.now());
    }
    
    /**
     * Randevu gelecek mi kontrol eder
     * @return boolean true ise randevu gelecek
     */
    public boolean isFuture() {
        return appointmentDateTime != null && appointmentDateTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * Randevu iptal edilebilir mi kontrol eder
     * Sadece beklemede olan ve gelecek tarihli randevular iptal edilebilir
     * @return boolean true ise iptal edilebilir
     */
    public boolean canBeCancelled() {
        return !cancelled && 
               status == RandevuDurumu.BEKLEMEDE && 
               isFuture();
    }
    
    /**
     * Randevu onaylanabilir mi kontrol eder (doktor için)
     * @return boolean true ise onaylanabilir
     */
    public boolean canBeApproved() {
        return !cancelled && 
               status == RandevuDurumu.BEKLEMEDE && 
               isFuture();
    }
    
    /**
     * toString metodu
     */
    @Override
    public String toString() {
        return "Randevu{" +
                "id=" + id +
                ", hasta=" + (hasta != null ? hasta.getFullName() : "null") +
                ", doktor=" + (doktor != null ? doktor.getFullName() : "null") +
                ", appointmentDateTime=" + appointmentDateTime +
                ", status=" + status +
                ", cancelled=" + cancelled +
                '}';
    }
} 