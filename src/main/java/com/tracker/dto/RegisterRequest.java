package com.tracker.dto;

import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private Set<String> roles;
    private String firstName;
    private String lastName;

    // Optional fields depending on role specific registration
    private String contactNumber;

    // Patient specific
    private String dateOfBirth; // parsing as string initially for simplicity

    // Doctor specific
    private String specialization;
    private String licenseNumber;

    // Pharmacist specific
    private String pharmacyName;
}
