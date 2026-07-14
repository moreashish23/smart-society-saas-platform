package com.smartsociety.analytics.service;

import com.smartsociety.analytics.dto.response.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AnalyticsService {

    DashboardResponse                   getDashboard(UUID societyId);

    ComplaintStatsResponse              getComplaintStats(UUID societyId, LocalDate from,
                                                          LocalDate to);

    List<DashboardResponse.TrendPoint>  getComplaintTrend(UUID societyId, int days);

    List<VendorPerformanceResponse>     getVendorPerformance(UUID societyId, LocalDate month);

    void                                refreshDailyStats(UUID societyId);

}