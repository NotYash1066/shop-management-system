package com.shopmanagement.services;

import com.shopmanagement.entity.AuditLog;
import com.shopmanagement.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Async
    public void log(String action, String entityName, Long entityId, Long userId, String details) {
        AuditLog log = new AuditLog(action, entityName, entityId, userId, details);
        auditLogRepository.save(log);
    }
}
