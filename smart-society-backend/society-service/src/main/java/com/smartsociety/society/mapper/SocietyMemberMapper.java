package com.smartsociety.society.mapper;

import com.smartsociety.society.dto.response.SocietyMemberResponse;
import com.smartsociety.society.entity.SocietyMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SocietyMemberMapper {

    @Mapping(target = "societyId",   source = "society.id")
    @Mapping(target = "societyName", source = "society.name")
    SocietyMemberResponse toResponse(SocietyMember member);
}