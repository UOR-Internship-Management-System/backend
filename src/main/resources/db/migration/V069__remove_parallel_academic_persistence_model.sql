DO $$
DECLARE
    duplicate_subject_count BIGINT := 0;
    duplicate_upload_count BIGINT := 0;
    duplicate_record_count BIGINT := 0;
BEGIN
    IF to_regclass('academic.subject') IS NULL
        OR to_regclass('academic.academic_ledger_upload') IS NULL
        OR to_regclass('academic.official_student_grade') IS NULL
        OR to_regclass('academic.student_academic_summary') IS NULL THEN
        RAISE EXCEPTION
            'V069 preflight failed: the authoritative academic persistence model is incomplete';
    END IF;

    IF to_regclass('public.subjects') IS NOT NULL THEN
        EXECUTE 'SELECT COUNT(*) FROM public.subjects' INTO duplicate_subject_count;
    END IF;

    IF to_regclass('public.academic_ledger_uploads') IS NOT NULL THEN
        EXECUTE 'SELECT COUNT(*) FROM public.academic_ledger_uploads' INTO duplicate_upload_count;
    END IF;

    IF to_regclass('public.academic_records') IS NOT NULL THEN
        EXECUTE 'SELECT COUNT(*) FROM public.academic_records' INTO duplicate_record_count;
    END IF;

    IF duplicate_subject_count > 0
        OR duplicate_upload_count > 0
        OR duplicate_record_count > 0 THEN
        RAISE EXCEPTION USING
            MESSAGE = format(
                'V069 preflight failed: parallel academic tables contain data (subjects=%s, uploads=%s, records=%s)',
                duplicate_subject_count,
                duplicate_upload_count,
                duplicate_record_count
            ),
            HINT = 'Reconcile these rows into the authoritative academic schema before retrying the migration.';
    END IF;
END
$$;

DROP TABLE IF EXISTS public.academic_records;
DROP TABLE IF EXISTS public.academic_ledger_uploads;
DROP TABLE IF EXISTS public.subjects;
