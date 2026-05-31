package com.DeltaEdge.controller;

import com.DeltaEdge.model.User;
import com.DeltaEdge.service.AuditService;
import com.DeltaEdge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserService userService;

    // Matches the frontend call: /api/audit?page=0&size=20
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
}