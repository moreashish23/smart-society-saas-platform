package com.smartsociety.society.mapper;

import com.smartsociety.society.dto.request.CreateSocietyRequest;
import com.smartsociety.society.dto.request.UpdateSocietyRequest;
import com.smartsociety.society.dto.response.SocietyResponse;
import com.smartsociety.society.entity.Society;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SocietyMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "status",    ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Society toEntity(CreateSocietyRequest request);


    @Mapping(target = "totalMembers", ignore = true)
    SocietyResponse toResponse(Society society);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "status",    ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromRequest(UpdateSocietyRequest request, @MappingTarget Society society);
}