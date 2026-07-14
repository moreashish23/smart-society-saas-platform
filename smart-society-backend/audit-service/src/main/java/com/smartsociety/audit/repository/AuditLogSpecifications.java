package com.smartsociety.audit.repository;

import com.smartsociety.audit.entity.AuditAction;
import com.smartsociety.audit.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;


public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> hasSocietyId(UUID societyId) {
        return (root, query, cb) ->
                societyId == null ? null : cb.equal(root.get("societyId"), societyId);
    }

    public static Specification<AuditLog> hasUserId(UUID userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("userId"), userId);
    }

    public static Specification<AuditLog> hasAction(AuditAction action) {
        return (root, query, cb) ->
                action == null ? null : cb.equal(root.get("action"), action);
    }

    public static Specification<AuditLog> hasEntityType(String entityType) {
        return (root, query, cb) ->
                entityType == null ? null : cb.equal(root.get("entityType"), entityType);
    }

    public static Specification<AuditLog> createdFrom(LocalDateTime from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<AuditLog> createdTo(LocalDateTime to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }


    public static Specification<AuditLog> search(UUID societyId, UUID userId, AuditAction action,
                                                 String entityType, LocalDateTime from, LocalDateTime to) {
        return Specification
                .where(hasSocietyId(societyId))
                .and(hasUserId(userId))
                .and(hasAction(action))
                .and(hasEntityType(entityType))
                .and(createdFrom(from))
                .and(createdTo(to));
    }
}