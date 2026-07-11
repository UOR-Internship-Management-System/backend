UPDATE user_accounts
SET password_hash = NULL,
    account_status = 'PASSWORD_SETUP_REQUIRED',
    password_changed_at = NULL,
    updated_at = now()
WHERE id = '00000000-0000-0000-0000-000000000001'
  AND university_email = 'admin@dcs.ruh.ac.lk'
  AND password_hash = '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiKO6ZC69s4V3QXKQ/8yDDDeWGPAHDy';
