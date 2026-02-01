package com.shopmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String entityName;
    private Long entityId;
    private Long userId;
    
    @Column(length = 1000)
    private String details;
    
    private LocalDateTime timestamp;

    public AuditLog(String action, String entityName, Long entityId, Long userId, String details) {
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.userId = userId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
