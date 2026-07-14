package com.smartsociety.complaint.mapper;

import com.smartsociety.complaint.dto.response.*;
import com.smartsociety.complaint.entity.*;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ComplaintMapper {

    @Mapping(target = "slaBreached", expression = "java(complaint.isSlaBreached())")
    @Mapping(target = "timeline",    ignore = true)
    @Mapping(target = "comments",    ignore = true)
    @Mapping(target = "attachments", ignore = true)
    ComplaintResponse toResponse(Complaint complaint);

    TimelineResponse   toTimelineResponse(ComplaintTimeline  timeline);
    CommentResponse    toCommentResponse(ComplaintComment    comment);
    AttachmentResponse toAttachmentResponse(ComplaintAttachment attachment);
}