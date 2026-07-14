package com.smartsociety.audit.mapper;

import com.smartsociety.audit.dto.response.AuditLogResponse;
import com.smartsociety.audit.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {
    AuditLogResponse toResponse(AuditLog auditLog);
}