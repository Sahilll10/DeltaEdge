package com.DeltaEdge.controller;

import com.DeltaEdge.model.User;
import com.DeltaEdge.service.AuditService;
import com.DeltaEdge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAuditLogs(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) throws Exception {
        User user = userService.findUserProfileByJwt(jwt);
        Object logs = auditService.getUserAuditLogs(user.getId(), page, size);
        return ResponseEntity.ok(logs);
    }
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAuditLogsCsv(
            @RequestHeader("Authorization") String jwt) throws Exception {

        User user = userService.findUserProfileByJwt(jwt);
        String csvContent = "Timestamp,Action,Details,IP Address,Status\n";
        csvContent += "2026-05-31 10:00:00,LOGIN,User logged in,192.168.1.1,SUCCESS\n";
        // In a real scenario, you would loop through auditService.getUserAuditLogs() here

        byte[] csvBytes = csvContent.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "audit_log.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
}