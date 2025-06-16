package com.example.randevusistemi.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

/**
 * Doktor entity sınıfı - User sınıfından türetilir
 * Doktor özel bilgilerini ve randevularını içerir
 */
@Entity
@Table(name = "doktorlar")
@PrimaryKeyJoinColumn(name = "user_id") // User tablosu ile join için
public class Doktor extends User {
    
    /**
     * Doktor diploma numarası - unique olmalı
     */
    @Column(name = "diploma_no", unique = true, length = 20)
    private String diplomaNo;
    
    /**
     * Uzmanlık alanı (Kardiyoloji, Nöroloji, vs.)
     */
    @Column(name = "specialization", nullable = false, length = 100)
    private String specialization;
    
    /**
     * Çalıştığı hastane/klinik
     */
    @Column(name = "hospital", length = 200)
    private String hospital;
    
    /**
     * Muayenehane adresi
     */
    @Column(name = "clinic_address", length = 500)
    private String clinicAddress;
    
    /**
     * Deneyim yılı
     */
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    /**
     * Randevu ücreti (TL)
     */
    @Column(name = "consultation_fee")
    private Double consultationFee;
    
    /**
     * Çalışma saati başlangıcı
     */
    @Column(name = "work_start_time")
    private LocalTime workStartTime;
    
    /**
     * Çalışma saati bitişi
     */
    @Column(name = "work_end_time")
    private LocalTime workEndTime;
    
    /**
     * Randevu süresi (dakika)
     */
    @Column(name = "appointment_duration")
    private Integer appointmentDuration = 30; // Varsayılan 30 dakika
    
    /**
     * Doktor hakkında bilgi
     */
    @Column(name = "about", length = 1000)
    private String about;
    
    /**
     * Aktif olarak randevu alıyor mu?
     */
    @Column(name = "accepting_appointments")
    private Boolean acceptingAppointments = true;
    
    /**
     * Doktor ile ilişkili randevular - One-to-Many ilişki
     * FetchType.LAZY: Randevular sadece ihtiyaç duyulduğunda yüklenir
     * mappedBy: Randevu entity'sindeki "doktor" field'ı ile eşleştirilir
     */
    @OneToMany(mappedBy = "doktor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Randevu> randevular;
    
    // Constructors
    /**
     * Varsayılan constructor
     */
    public Doktor() {
        super();
    }
    
    /**
     * Parametreli constructor
     * @param username Kullanıcı adı
     * @param password Şifre
     * @param firstName Ad
     * @param lastName Soyad
     * @param email E-posta
     * @param diplomaNo Diploma numarası
     * @param specialization Uzmanlık alanı
     */
    public Doktor(String username, String password, String firstName, String lastName, 
                  String email, String diplomaNo, String specialization) {
        super(username, password, firstName, lastName, email, com.example.randevusistemi.enums.Role.DOKTOR);
        this.diplomaNo = diplomaNo;
        this.specialization = specialization;
    }
    
    // Getters and Setters
    public String getDiplomaNo() { return diplomaNo; }
    public void setDiplomaNo(String diplomaNo) { this.diplomaNo = diplomaNo; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    
    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }
    
    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    
    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }
    
    public LocalTime getWorkStartTime() { return workStartTime; }
    public void setWorkStartTime(LocalTime workStartTime) { this.workStartTime = workStartTime; }
    
    public LocalTime getWorkEndTime() { return workEndTime; }
    public void setWorkEndTime(LocalTime workEndTime) { this.workEndTime = workEndTime; }
    
    public Integer getAppointmentDuration() { return appointmentDuration; }
    public void setAppointmentDuration(Integer appointmentDuration) { this.appointmentDuration = appointmentDuration; }
    
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
    
    public Boolean getAcceptingAppointments() { return acceptingAppointments; }
    public void setAcceptingAppointments(Boolean acceptingAppointments) { this.acceptingAppointments = acceptingAppointments; }
    
    public List<Randevu> getRandevular() { return randevular; }
    public void setRandevular(List<Randevu> randevular) { this.randevular = randevular; }
    
    /**
     * Doktor unvanı ile birlikte tam adını döndürür
     * @return String Dr. Ad Soyad formatında
     */
    public String getDoctorTitle() {
        return "Dr. " + getFullName();
    }
    
    /**
     * Çalışma saatleri string formatında döndürür
     * @return String "09:00 - 17:00" formatında
     */
    public String getWorkHours() {
        if (workStartTime != null && workEndTime != null) {
            return workStartTime.toString() + " - " + workEndTime.toString();
        }
        return "Belirtilmemiş";
    }
    
    /**
     * toString metodu
     */
    @Override
    public String toString() {
        return "Doktor{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", specialization='" + specialization + '\'' +
                ", diplomaNo='" + diplomaNo + '\'' +
                ", hospital='" + hospital + '\'' +
                '}';
    }
} 