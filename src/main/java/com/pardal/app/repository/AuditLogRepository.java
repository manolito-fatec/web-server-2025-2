package com.pardal.app.repository;

import com.pardal.app.entity.AuditLog;
import org.springframework.data.repository.Repository;

public interface AuditLogRepository extends Repository<AuditLog, Long> {
}