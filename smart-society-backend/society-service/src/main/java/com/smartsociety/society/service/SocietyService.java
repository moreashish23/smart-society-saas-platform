package com.smartsociety.society.service;

import com.smartsociety.society.dto.request.CreateSocietyRequest;
import com.smartsociety.society.dto.request.UpdateSocietyRequest;
import com.smartsociety.society.dto.response.PagedResponse;
import com.smartsociety.society.dto.response.SocietyResponse;
import com.smartsociety.society.entity.SocietyStatus;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface SocietyService {
    SocietyResponse             createSociety(CreateSocietyRequest request, UUID createdBy);
    SocietyResponse             getSociety(UUID societyId);
    SocietyResponse             updateSociety(UUID societyId, UpdateSocietyRequest request, UUID requesterId);
    PagedResponse<SocietyResponse> getAllSocieties(SocietyStatus status, String name, Pageable pageable);
    SocietyResponse             activateSociety(UUID societyId, UUID requesterId);
    SocietyResponse             deactivateSociety(UUID societyId, UUID requesterId);
    void                        deleteSociety(UUID societyId, UUID requesterId);
}