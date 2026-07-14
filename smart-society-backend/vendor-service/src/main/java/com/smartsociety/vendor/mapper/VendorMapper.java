package com.smartsociety.vendor.mapper;

import com.smartsociety.vendor.dto.request.CreateVendorRequest;
import com.smartsociety.vendor.dto.request.UpdateVendorRequest;
import com.smartsociety.vendor.dto.response.VendorJobResponse;
import com.smartsociety.vendor.dto.response.VendorRatingResponse;
import com.smartsociety.vendor.dto.response.VendorResponse;
import com.smartsociety.vendor.entity.Vendor;
import com.smartsociety.vendor.entity.VendorJob;
import com.smartsociety.vendor.entity.VendorRating;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VendorMapper {

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "societyId",    ignore = true)
    @Mapping(target = "rating",       ignore = true)
    @Mapping(target = "totalJobs",    ignore = true)
    @Mapping(target = "completedJobs",ignore = true)
    @Mapping(target = "status",       ignore = true)
    @Mapping(target = "approvedBy",   ignore = true)
    @Mapping(target = "approvedAt",   ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    Vendor toEntity(CreateVendorRequest request);

    VendorResponse toResponse(Vendor vendor);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "societyId",    ignore = true)
    @Mapping(target = "userId",       ignore = true)
    @Mapping(target = "rating",       ignore = true)
    @Mapping(target = "totalJobs",    ignore = true)
    @Mapping(target = "completedJobs",ignore = true)
    @Mapping(target = "status",       ignore = true)
    @Mapping(target = "approvedBy",   ignore = true)
    @Mapping(target = "approvedAt",   ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    void updateFromRequest(UpdateVendorRequest request, @MappingTarget Vendor vendor);

    @Mapping(target = "vendorId", source = "vendor.id")
    VendorJobResponse toJobResponse(VendorJob job);

    @Mapping(target = "vendorId", source = "vendor.id")
    VendorRatingResponse toRatingResponse(VendorRating rating);
}