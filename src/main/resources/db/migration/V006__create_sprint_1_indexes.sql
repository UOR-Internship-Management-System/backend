CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_eligible_students_level ON eligible_students(academic_level);
CREATE INDEX idx_verification_sessions_student ON verification_sessions(eligible_student_id);
CREATE INDEX idx_verification_sessions_identity ON verification_sessions(index_number, university_email);
CREATE INDEX idx_verification_sessions_expires_at ON verification_sessions(expires_at);
CREATE INDEX idx_audit_events_actor_user_id ON audit_events(actor_user_id);
CREATE INDEX idx_audit_events_type_occurred_at ON audit_events(event_type, occurred_at DESC);
CREATE INDEX idx_audit_events_correlation_id ON audit_events(correlation_id);
