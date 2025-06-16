package com.example.randevusistemi.enums;

/**
 * Kullanıcı rollerini tanımlayan enum sınıfı
 * Sistemde iki tür kullanıcı bulunur: HASTA ve DOKTOR
 */
public enum Role {
    /**
     * Hasta rolü - randevu alabilir, geçmiş randevularını görüntüleyebilir
     */
    HASTA("Hasta"),
    
    /**
     * Doktor rolü - randevuları yönetebilir, onaylayabilir/reddedebilir, not ekleyebilir
     */
    DOKTOR("Doktor");
    
    private final String displayName;
    
    /**
     * Role enum constructor
     * @param displayName Görüntüleme adı
     */
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Rolün görüntüleme adını döndürür
     * @return String görüntüleme adı
     */
    public String getDisplayName() {
        return displayName;
    }
} 