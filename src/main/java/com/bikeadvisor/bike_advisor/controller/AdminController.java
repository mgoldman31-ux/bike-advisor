package com.bikeadvisor.bike_advisor.controller;

import com.bikeadvisor.bike_advisor.service.DataRefreshService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final DataRefreshService dataRefreshService;

    public AdminController(DataRefreshService dataRefreshService) {
        this.dataRefreshService = dataRefreshService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh() {
        try {
            dataRefreshService.refresh();
            return ResponseEntity.ok("Refresh complete.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Refresh failed: " + e.getMessage());
        }
    }

    @PostMapping("/runPca")
    public ResponseEntity<String> runPCA() {
        try {
            String summary = dataRefreshService.refreshPca();
            return ResponseEntity.ok("PCA complete.\n\n" + summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Run PCA failed: " + e.getMessage());
        }
    }
}
