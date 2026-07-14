package com.smartsociety.notification.mapper;

import com.smartsociety.notification.dto.response.NotificationResponse;
import com.smartsociety.notification.entity.Notification;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper {

    @Mapping(target = "read", source = "read")
    NotificationResponse toResponse(Notification notification);
}