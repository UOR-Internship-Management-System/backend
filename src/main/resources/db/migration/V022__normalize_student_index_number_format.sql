UPDATE eligible_students
SET index_number = split_part(index_number, '-', 1)
        || '/' || split_part(index_number, '-', 2)
        || '/' || lpad(split_part(index_number, '-', 3), 5, '0')
WHERE index_number ~ '^[A-Za-z]{2}-[0-9]{4}-[0-9]{3}$';

UPDATE verification_sessions
SET index_number = split_part(index_number, '-', 1)
        || '/' || split_part(index_number, '-', 2)
        || '/' || lpad(split_part(index_number, '-', 3), 5, '0')
WHERE index_number ~ '^[A-Za-z]{2}-[0-9]{4}-[0-9]{3}$';

ALTER TABLE eligible_students
    ADD CONSTRAINT chk_eligible_students_index_number_format
        CHECK (index_number ~ '^[A-Za-z]{2}/[0-9]{4}/[0-9]{5}$');
