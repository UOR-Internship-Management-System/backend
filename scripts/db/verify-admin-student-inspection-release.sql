-- Read-only Admin Student Inspection release verification.
-- Run against the dedicated acceptance database after the Newman/live-E2E flow.

SELECT version, description, success
FROM public.flyway_schema_history
ORDER BY installed_rank DESC
LIMIT 10;

SELECT COUNT(*) AS active_registered_students
FROM public.eligible_students es
JOIN public.user_accounts ua ON ua.id = es.user_account_id
WHERE ua.account_status = 'ACTIVE';

SELECT status, COUNT(*) AS cv_count
FROM public.cvs
GROUP BY status
ORDER BY status;

SELECT COUNT(*) AS active_saved_cv_count
FROM public.cvs
WHERE status = 'SAVED'
  AND is_active = TRUE;

SELECT event_type, event_category, resource_type, COUNT(*) AS event_count
FROM public.audit_events
WHERE event_type IN ('CV_DOWNLOADED_BY_ADMIN', 'CV_FILE_UNAVAILABLE')
GROUP BY event_type, event_category, resource_type
ORDER BY event_type, resource_type;

SELECT COUNT(*) AS forbidden_admin_student_mutation_routes
FROM information_schema.routines
WHERE routine_schema = 'public'
  AND routine_name ILIKE '%admin%student%update%';
