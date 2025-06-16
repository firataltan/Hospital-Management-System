package com.example.randevusistemi.enums;

/**
 * Randevu durumlarını tanımlayan enum sınıfı
 * Randevu süreci boyunca farklı durumları temsil eder
 */
public enum RandevuDurumu {
    /**
     * Beklemede - Randevu alındı ancak doktor tarafından henüz onaylanmadı
     */
    BEKLEMEDE("Beklemede", "warning"),
    
    /**
     * Onaylandı - Doktor randevuyu onayladı
     */
    ONAYLANDI("Onaylandı", "success"),
    
    /**
     * Reddedildi - Doktor randevuyu reddetti
     */
    REDDEDILDI("Reddedildi", "danger");
    
    private final String displayName;
    private final String cssClass; // Bootstrap CSS sınıfları için
    
    /**
     * RandevuDurumu enum constructor
     * @param displayName Görüntüleme adı
     * @param cssClass CSS sınıfı (Bootstrap renk kodları)
     */
    RandevuDurumu(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }
    
    /**
     * Durumun görüntüleme adını döndürür
     * @return String görüntüleme adı
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * CSS sınıfını döndürür (UI renklendirme için)
     * @return String CSS sınıfı
     */
    public String getCssClass() {
        return cssClass;
    }
} 