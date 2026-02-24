package com.bikeadvisor.bike_advisor.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;

@RestController
@RequestMapping("/download")
public class DownloadController {

    private static final Path RIDE_CHARACTERS_PATH = Path.of("ride-characters.csv");

    @GetMapping("/ride-characters")
    public ResponseEntity<Resource> downloadRideCharacters() throws FileNotFoundException {
        var file = RIDE_CHARACTERS_PATH.toFile();
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        var resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ride-characters.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(file.length())
                .body(resource);
    }
}