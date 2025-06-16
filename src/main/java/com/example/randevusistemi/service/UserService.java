package com.example.randevusistemi.service;

import com.example.randevusistemi.entity.User;
import com.example.randevusistemi.entity.Hasta;
import com.example.randevusistemi.entity.Doktor;
import com.example.randevusistemi.enums.Role;
import com.example.randevusistemi.repository.UserRepository;
import com.example.randevusistemi.repository.HastaRepository;
import com.example.randevusistemi.repository.DoktorRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Kullanıcı yönetimi için service sınıfı
 * Spring Security entegrasyonu ve kullanıcı CRUD işlemleri
 */
@Service
@Transactional
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Spring Security için kullanıcı detaylarını yükler
     * Authentication işlemi sırasında çağrılır
     * @param username Kullanıcı adı
     * @return UserDetails Spring Security için kullanıcı bilgileri
     * @throws UsernameNotFoundException Kullanıcı bulunamadığında
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
        
        // Aktif olmayan kullanıcılar giriş yapamaz
        if (!user.getActive()) {
            throw new UsernameNotFoundException("Kullanıcı hesabı devre dışı: " + username);
        }
        
        // Spring Security UserDetails objesi oluştur
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
    
    /**
     * Kullanıcı adına göre kullanıcı bulur
     * @param username Kullanıcı adı
     * @return Optional<User> kullanıcı varsa User objesi
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * ID'ye göre kullanıcı bulur
     * @param id Kullanıcı ID
     * @return Optional<User> kullanıcı varsa User objesi
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Email adresine göre kullanıcı bulur
     * @param email Email adresi
     * @return Optional<User> kullanıcı varsa User objesi
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Kullanıcı adı zaten kullanılıyor mu kontrol eder
     * @param username Kullanıcı adı
     * @return boolean true ise zaten mevcut
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Email zaten kullanılıyor mu kontrol eder
     * @param email Email adresi
     * @return boolean true ise zaten mevcut
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Kullanıcı şifresini günceller
     * @param user Kullanıcı
     * @param newPassword Yeni şifre (plain text)
     * @return User güncellenmiş kullanıcı
     */
    public User updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
    
    /**
     * Kullanıcıyı aktif/pasif yapar
     * @param userId Kullanıcı ID
     * @param active Aktiflik durumu
     * @return boolean işlem başarılı mı
     */
    public boolean toggleUserStatus(Long userId, boolean active) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(active);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    /**
     * Role göre kullanıcıları listeler
     * @param role Kullanıcı rolü
     * @return List<User> belirtilen roldeki kullanıcılar
     */
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Aktif kullanıcıları listeler
     * @param active Aktiflik durumu
     * @return List<User> aktif/pasif kullanıcılar
     */
    public List<User> findByActive(boolean active) {
        return userRepository.findByActive(active);
    }
    
    /**
     * Tüm kullanıcıları listeler
     * @return List<User> tüm kullanıcılar
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    /**
     * Kullanıcı profilini günceller
     * @param user Güncellenecek kullanıcı
     * @return User güncellenmiş kullanıcı
     */
    public User updateProfile(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Kullanıcıyı siler
     * @param userId Kullanıcı ID
     * @return boolean silme işlemi başarılı mı
     */
    public boolean deleteUser(Long userId) {
        try {
            userRepository.deleteById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * İsime göre kullanıcı arar
     * @param name Aranacak isim
     * @return List<User> isim eşleşen kullanıcılar
     */
    public List<User> searchByName(String name) {
        return userRepository.findByFullNameContainingIgnoreCase(name);
    }
    
    /**
     * Şifre doğrulama kontrolü
     * @param rawPassword Ham şifre
     * @param encodedPassword Kodlanmış şifre
     * @return boolean şifre doğru mu
     */
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
} 