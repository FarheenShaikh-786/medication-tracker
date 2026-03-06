package com.tracker.service;

import com.tracker.dto.LoginRequest;
import com.tracker.dto.RegisterRequest;
import com.tracker.dto.JwtResponse;
import com.tracker.dto.MessageResponse;
import com.tracker.entity.*;
import com.tracker.repository.*;
import com.tracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PharmacistProfileRepository pharmacistProfileRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User not found"));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);
    }

    @Transactional
    public MessageResponse registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();
        String primaryRole = "";

        if (strRoles == null) {
            Role userRole = roleRepository.findByName("PATIENT")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            primaryRole = "PATIENT";
        } else {
            for (String role : strRoles) {
                switch (role.toUpperCase()) {
                    case "ADMIN":
                        Role adminRole = roleRepository.findByName("ADMIN")
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        primaryRole = primaryRole.isEmpty() ? "ADMIN" : primaryRole;
                        break;
                    case "DOCTOR":
                        Role modRole = roleRepository.findByName("DOCTOR")
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                        primaryRole = "DOCTOR";
                        break;
                    case "PHARMACIST":
                        Role pharmRole = roleRepository.findByName("PHARMACIST")
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(pharmRole);
                        primaryRole = "PHARMACIST";
                        break;
                    default:
                        Role userRole = roleRepository.findByName("PATIENT")
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                        primaryRole = "PATIENT";
                }
            }
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Map profiles based on roles
        if (primaryRole.equals("PATIENT")) {
            PatientProfile profile = new PatientProfile();
            profile.setUser(savedUser);
            profile.setFirstName(signUpRequest.getFirstName());
            profile.setLastName(signUpRequest.getLastName());
            profile.setContactNumber(signUpRequest.getContactNumber());
            if (signUpRequest.getDateOfBirth() != null) {
                profile.setDateOfBirth(LocalDate.parse(signUpRequest.getDateOfBirth())); // simple iso parsing
            }
            patientProfileRepository.save(profile);
        } else if (primaryRole.equals("DOCTOR")) {
            DoctorProfile profile = new DoctorProfile();
            profile.setUser(savedUser);
            profile.setFirstName(signUpRequest.getFirstName());
            profile.setLastName(signUpRequest.getLastName());
            profile.setSpecialization(signUpRequest.getSpecialization());
            profile.setLicenseNumber(signUpRequest.getLicenseNumber());
            profile.setContactNumber(signUpRequest.getContactNumber());
            doctorProfileRepository.save(profile);
        } else if (primaryRole.equals("PHARMACIST")) {
            PharmacistProfile profile = new PharmacistProfile();
            profile.setUser(savedUser);
            profile.setFirstName(signUpRequest.getFirstName());
            profile.setLastName(signUpRequest.getLastName());
            profile.setPharmacyName(signUpRequest.getPharmacyName());
            profile.setContactNumber(signUpRequest.getContactNumber());
            pharmacistProfileRepository.save(profile);
        }

        return new MessageResponse("User registered successfully!");
    }
}
