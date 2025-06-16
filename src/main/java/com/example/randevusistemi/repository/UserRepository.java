package com.example.randevusistemi.repository;

import com.example.randevusistemi.entity.User;
import com.example.randevusistemi.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User entity için repository interface
 * Spring Data JPA kullanarak temel CRUD işlemlerini ve özel sorguları sağlar
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Kullanıcı adına göre kullanıcı bulur
     * Spring Security authentication için kullanılır
     * @param username Kullanıcı adı
     * @return Optional<User> kullanıcı varsa User, yoksa empty
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Email adresine göre kullanıcı bulur
     * Unique email kontrolü için kullanılır
     * @param email Email adresi
     * @return Optional<User> kullanıcı varsa User, yoksa empty
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Kullanıcı adı var mı kontrol eder
     * Kayıt sırasında unique kontrolü için kullanılır
     * @param username Kullanıcı adı
     * @return boolean true ise kullanıcı adı zaten mevcut
     */
    boolean existsByUsername(String username);
    
    /**
     * Email var mı kontrol eder
     * Kayıt sırasında unique kontrolü için kullanılır
     * @param email Email adresi
     * @return boolean true ise email zaten mevcut
     */
    boolean existsByEmail(String email);
    
    /**
     * Role göre kullanıcıları bulur
     * Admin paneli gibi yerlerde role bazlı listeleme için
     * @param role Kullanıcı rolü (HASTA veya DOKTOR)
     * @return List<User> belirtilen role sahip kullanıcılar
     */
    List<User> findByRole(Role role);
    
    /**
     * Aktif kullanıcıları bulur
     * Sadece aktif kullanıcıları listelemek için
     * @param active Aktiflik durumu
     * @return List<User> aktif/pasif kullanıcılar
     */
    List<User> findByActive(Boolean active);
    
    /**
     * Role ve aktiflik durumuna göre kullanıcıları bulur
     * @param role Kullanıcı rolü
     * @param active Aktiflik durumu
     * @return List<User> kriterlere uyan kullanıcılar
     */
    List<User> findByRoleAndActive(Role role, Boolean active);
    
    /**
     * Ad veya soyada göre arama yapar (case-insensitive)
     * Kullanıcı arama fonksiyonu için
     * @param firstName Ad
     * @param lastName Soyad
     * @return List<User> ad veya soyad eşleşen kullanıcılar
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            @Param("firstName") String firstName, 
            @Param("lastName") String lastName);
    
    /**
     * Tam ad ile arama yapar (ad + soyad birleştirme)
     * @param fullName Tam ad
     * @return List<User> tam ad eşleşen kullanıcılar
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    List<User> findByFullNameContainingIgnoreCase(@Param("fullName") String fullName);
} 