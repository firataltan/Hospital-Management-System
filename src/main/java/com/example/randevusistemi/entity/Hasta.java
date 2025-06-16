package com.example.randevusistemi.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Hasta entity sınıfı - User sınıfından türetilir
 * Hasta özel bilgilerini ve randevularını içerir
 */
@Entity
@Table(name = "hastalar")
@PrimaryKeyJoinColumn(name = "user_id") // User tablosu ile join için
public class Hasta extends User {
    
    /**
     * T.C. Kimlik numarası - unique olmalı
     */
    @Column(name = "tc_no", unique = true, length = 11)
    private String tcNo;
    
    /**
     * Doğum tarihi
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    /**
     * Cinsiyet
     */
    @Column(name = "gender", length = 10)
    private String gender;
    
    /**
     * Adres bilgisi
     */
    @Column(name = "address", length = 500)
    private String address;
    
    /**
     * Acil durum iletişim kişisi
     */
    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;
    
    /**
     * Acil durum telefonu
     */
    @Column(name = "emergency_phone", length = 15)
    private String emergencyPhone;
    
    /**
     * Hasta ile ilişkili randevular - One-to-Many ilişki
     * FetchType.LAZY: Randevular sadece ihtiyaç duyulduğunda yüklenir
     * mappedBy: Randevu entity'sindeki "hasta" field'ı ile eşleştirilir
     */
    @OneToMany(mappedBy = "hasta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Randevu> randevular;
    
    // Constructors
    /**
     * Varsayılan constructor
     */
    public Hasta() {
        super();
    }
    
    /**
     * Parametreli constructor
     * @param username Kullanıcı adı
     * @param password Şifre
     * @param firstName Ad
     * @param lastName Soyad
     * @param email E-posta
     * @param tcNo T.C. Kimlik No
     */
    public Hasta(String username, String password, String firstName, String lastName, String email, String tcNo) {
        super(username, password, firstName, lastName, email, com.example.randevusistemi.enums.Role.HASTA);
        this.tcNo = tcNo;
    }
    
    // Getters and Setters
    public String getTcNo() { return tcNo; }
    public void setTcNo(String tcNo) { this.tcNo = tcNo; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    
    public String getEmergencyPhone() { return emergencyPhone; }
    public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }
    
    public List<Randevu> getRandevular() { return randevular; }
    public void setRandevular(List<Randevu> randevular) { this.randevular = randevular; }
    
    /**
     * Hastanın yaşını hesaplar
     * @return int yaş
     */
    public int getAge() {
        if (birthDate == null) return 0;
        return LocalDate.now().getYear() - birthDate.getYear();
    }
    
    /**
     * toString metodu
     */
    @Override
    public String toString() {
        return "Hasta{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", tcNo='" + tcNo + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
} 