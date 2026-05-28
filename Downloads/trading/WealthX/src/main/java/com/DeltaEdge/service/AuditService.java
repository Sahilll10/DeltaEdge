package com.DeltaEdge.service;

import com.DeltaEdge.model.AuditLog;
import com.DeltaEdge.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(Long userId, String action, String details, String status, HttpServletRequest request) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setDetails(details);
        log.setStatus(status);
        log.setIpAddress(request.getRemoteAddr());
        auditLogRepository.save(log);
    }
}