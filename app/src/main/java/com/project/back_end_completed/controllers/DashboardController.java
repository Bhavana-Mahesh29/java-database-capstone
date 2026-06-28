package com.project.back_end_completed.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end_completed.services.TokenService;

@Controller
public class DashboardController {

    @Autowired
    private TokenService TokenService;

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable("token") String token) {

        Map<String, String> validation = TokenService.validateToken(token, "admin");

        if (validation.isEmpty()) {
            return "admin/adminDashboard";
        }

        return "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable("token") String token) {

        Map<String, String> validation = TokenService.validateToken(token, "doctor");

        if (validation.isEmpty()) {
            return "doctor/doctorDashboard";
        }

        return "redirect:/";
    }

}

