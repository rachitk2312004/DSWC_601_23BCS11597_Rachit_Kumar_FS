-- Q5: Enterprise Job Queue

SELECT b.job_id, d.dept_name, b.created_at
FROM background_jobs b
INNER JOIN departments d
    ON b.dept_id = d.dept_id
WHERE b.status = 'PENDING'
  AND d.dept_name = 'Engineering'
ORDER BY b.created_at ASC
FOR UPDATE SKIP LOCKED
LIMIT 1;

CREATE INDEX idx_background_jobs_pending_partial
ON background_jobs (dept_id, created_at)
WHERE status = 'PENDING';
