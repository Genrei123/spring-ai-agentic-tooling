package com.multi_ai_agent_config.multi_agent_config.Controllers;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
public class ReportController {

    private static final Path EXPORT_DIR = Path.of("exports");

    @GetMapping("/reports/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        // Reject anything with path traversal characters before touching the filesystem
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = EXPORT_DIR.resolve(filename).normalize();
        if (!filePath.startsWith(EXPORT_DIR) || !filePath.toFile().exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}