package com.project.back_end_completed.services;

import com.project.back_end_completed.repo.AdminRepository;
import com.project.back_end_completed.repo.DoctorRepository;
import com.project.back_end_completed.repo.PatientRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public Map<String, String> validateToken(String token, String role) {
        Map<String, String> errors = new HashMap<>();
        try {
            String email = extractEmail(token);
            boolean valid = switch (role.toLowerCase()) {
                case "admin" -> adminRepository.findByUsername(email) != null;
                case "doctor" -> doctorRepository.findByEmail(email) != null;
                case "patient" -> patientRepository.findByEmail(email) != null;
                default -> false;
            };
            if (!valid) {
                errors.put("message", "Unauthorized: invalid token for role " + role);
            }
        } catch (Exception e) {
            errors.put("message", "Unauthorized: " + e.getMessage());
        }
        return errors;
    }
}
