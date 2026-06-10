-- Q4: Himalayan Fleet Tracker

SELECT rider_id, rider_name, bike_model, latitude, longitude, recorded_at
FROM (
    SELECT
        r.rider_id,
        r.rider_name,
        r.bike_model,
        g.latitude,
        g.longitude,
        g.recorded_at,
        ROW_NUMBER() OVER (
            PARTITION BY r.rider_id
            ORDER BY g.recorded_at DESC
        ) AS rn
    FROM riders r
    INNER JOIN gps_pings g
        ON r.rider_id = g.rider_id
) latest_ping
WHERE rn = 1;

CREATE INDEX idx_gps_pings_rider_recorded_at
ON gps_pings (rider_id, recorded_at DESC);
