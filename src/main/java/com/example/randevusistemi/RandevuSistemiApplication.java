package com.example.randevusistemi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Randevu ve Hasta Takip Sistemi - Ana Uygulama Sınıfı
 * Bu sınıf Spring Boot uygulamasının başlangıç noktasıdır
 * @SpringBootApplication annotation'ı ile otomatik konfigürasyon etkinleştirilir
 */
@SpringBootApplication
public class RandevuSistemiApplication {

    /**
     * Uygulamanın main metodu - Spring Boot uygulamasını başlatır
     * @param args Komut satırı argümanları
     */
    public static void main(String[] args) {
        SpringApplication.run(RandevuSistemiApplication.class, args);
        System.out.println("Randevu Sistemi başarıyla başlatıldı!");
        System.out.println("Uygulama URL: http://localhost:8080");
    }
} 