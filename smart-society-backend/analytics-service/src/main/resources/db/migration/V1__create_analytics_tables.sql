CREATE TABLE IF NOT EXISTS daily_complaint_stats (
    id                   UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    society_id           UUID         NOT NULL,
    stat_date            DATE         NOT NULL,
    total_complaints     INT          NOT NULL DEFAULT 0,
    open_complaints      INT          NOT NULL DEFAULT 0,
    closed_complaints    INT          NOT NULL DEFAULT 0,
    reopened_complaints  INT          NOT NULL DEFAULT 0,
    critical_complaints  INT          NOT NULL DEFAULT 0,
    high_complaints      INT          NOT NULL DEFAULT 0,
    medium_complaints    INT          NOT NULL DEFAULT 0,
    low_complaints       INT          NOT NULL DEFAULT 0,
    sla_breaches         INT          NOT NULL DEFAULT 0,
    avg_resolution_hours NUMERIC(10,2) NOT NULL DEFAULT 0,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (society_id, stat_date)
    );

CREATE TABLE IF NOT EXISTS vendor_performance_stats (
    id             UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    vendor_id      UUID         NOT NULL,
    society_id     UUID         NOT NULL,
    vendor_name    VARCHAR(255),
    stat_month     DATE         NOT NULL,
    jobs_assigned  INT          NOT NULL DEFAULT 0,
    jobs_completed INT          NOT NULL DEFAULT 0,
    jobs_cancelled INT          NOT NULL DEFAULT 0,
    avg_rating     NUMERIC(3,2) NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (vendor_id, stat_month)
    );

CREATE INDEX IF NOT EXISTS idx_daily_stats_society   ON daily_complaint_stats(society_id);
CREATE INDEX IF NOT EXISTS idx_daily_stats_date      ON daily_complaint_stats(stat_date DESC);
CREATE INDEX IF NOT EXISTS idx_vendor_stats_vendor   ON vendor_performance_stats(vendor_id);
CREATE INDEX IF NOT EXISTS idx_vendor_stats_society  ON vendor_performance_stats(society_id);
CREATE INDEX IF NOT EXISTS idx_vendor_stats_month    ON vendor_performance_stats(stat_month DESC);