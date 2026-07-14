package com.smartsociety.society.mapper;

import com.smartsociety.society.dto.request.CreateNoticeRequest;
import com.smartsociety.society.dto.request.UpdateNoticeRequest;
import com.smartsociety.society.dto.response.NoticeResponse;
import com.smartsociety.society.entity.Notice;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NoticeMapper {

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "society",     ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    Notice toEntity(CreateNoticeRequest request);

    @Mapping(target = "societyId",   source = "society.id")
    @Mapping(target = "societyName", source = "society.name")
    NoticeResponse toResponse(Notice notice);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "society",     ignore = true)
    @Mapping(target = "status",      ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    void updateFromRequest(UpdateNoticeRequest request, @MappingTarget Notice notice);
}