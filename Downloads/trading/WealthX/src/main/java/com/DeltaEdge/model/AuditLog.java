package com.DeltaEdge.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String action;
    private String details;
    private Long userId;
    private String ipAddress;
    private String status;
    private LocalDateTime timestamp = LocalDateTime.now();
}