package com.example.hospital_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.hospital_management_system", "com.example.randevusistemi"})
public class OspitalManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(OspitalManagementSystemApplication.class, args);
	}

}
