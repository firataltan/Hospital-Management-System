package com.example.randevusistemi.entity;

import com.example.randevusistemi.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Temel kullanıcı entity sınıfı
 * Hem hasta hem de doktor kullanıcıları için ortak alanları içerir
 * Bu sınıf inheritance hierarchy'nin base sınıfıdır
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED) // Joined table stratejisi kullanılır
public abstract class User {
    
    /**
     * Birincil anahtar - otomatik artan ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    
    /**
     * Kullanıcı adı - unique olmalı, giriş için kullanılır
     */
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    /**
     * Şifre - hash'lenmiş olarak saklanır
     */
    @Column(name = "password", nullable = false)
    private String password;
    
    /**
     * Ad
     */
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    /**
     * Soyad
     */
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    /**
     * E-posta adresi - unique olmalı
     */
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    /**
     * Telefon numarası
     */
    @Column(name = "phone", length = 15)
    private String phone;
    
    /**
     * Kullanıcı rolü - HASTA veya DOKTOR
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    /**
     * Hesap aktif mi? - kullanıcı devre dışı bırakma için
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    /**
     * Kayıt tarihi - hesap oluşturulma zamanı
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Güncelleme tarihi - son değişiklik zamanı
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Entity persist edilmeden önce çalışır - kayıt tarihi set edilir
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Entity update edilmeden önce çalışır - güncelleme tarihi set edilir
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    /**
     * Varsayılan constructor
     */
    public User() {}
    
    /**
     * Parametreli constructor
     * @param username Kullanıcı adı
     * @param password Şifre
     * @param firstName Ad
     * @param lastName Soyad
     * @param email E-posta
     * @param role Rol
     */
    public User(String username, String password, String firstName, String lastName, String email, Role role) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    /**
     * Tam adı döndürür (Ad + Soyad)
     * @return String tam ad
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    /**
     * toString metodu - debug için kullanışlı
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
} 