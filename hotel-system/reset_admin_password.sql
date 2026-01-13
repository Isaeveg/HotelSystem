DO $$
DECLARE
    admin_count INTEGER;
    hashed_password TEXT := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
BEGIN
    SELECT COUNT(*) INTO admin_count FROM users WHERE username = 'admin';
    
    IF admin_count > 0 THEN
        UPDATE users SET password = hashed_password WHERE username = 'admin';
        RAISE NOTICE 'Hasło użytkownika admin zaktualizowane';
    ELSE
        INSERT INTO users (username, password, role) VALUES ('admin', hashed_password, 'admin');
        RAISE NOTICE 'Użytkownik admin został utworzony';
    END IF;
END $$;

SELECT id, username, role, 
       CASE 
           WHEN password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
           THEN 'Hasło ustawione poprawnie (admin)'
           ELSE 'UWAGA: Hasło różni się od oczekiwanego'
       END as password_status
FROM users 
WHERE username = 'admin';
