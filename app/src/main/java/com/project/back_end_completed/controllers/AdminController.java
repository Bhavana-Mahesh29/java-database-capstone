package com.project.back_end_completed.controllers;

import com.project.back_end_completed.models.Admin;
import com.project.back_end_completed.services.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.path}admin")
public class AdminController {

    private final Service service;

    public AdminController(Service service) {
        this.service = service;
    }

    // POST ${api.path}admin/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return service.validateAdmin(admin.getUsername(), admin.getPassword());
    }
}
