-- Q1: Logistics Bottleneck (CargoLogix System)

SELECT
    s.shipment_id,
    c.company_name,
    s.dispatch_date
FROM shipments s
INNER JOIN couriers c
    ON s.courier_id = c.courier_id
WHERE s.status = 'DELAYED'
ORDER BY s.dispatch_date DESC;

CREATE INDEX idx_shipments_status_dispatch_date
ON shipments (status, dispatch_date DESC);
