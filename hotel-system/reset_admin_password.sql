-- SQL скрипт для сброса пароля администратора
-- Пароль: admin
-- Хеш создан с помощью BCrypt

-- Сначала проверяем, существует ли пользователь admin
DO $$
DECLARE
    admin_count INTEGER;
    -- Хеш для пароля "admin" (BCrypt)
    hashed_password TEXT := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
BEGIN
    SELECT COUNT(*) INTO admin_count FROM users WHERE username = 'admin';
    
    IF admin_count > 0 THEN
        -- Если пользователь существует, обновляем пароль
        UPDATE users SET password = hashed_password WHERE username = 'admin';
        RAISE NOTICE 'Пароль пользователя admin обновлён';
    ELSE
        -- Если не существует, создаём пользователя
        INSERT INTO users (username, password, role) VALUES ('admin', hashed_password, 'admin');
        RAISE NOTICE 'Пользователь admin создан';
    END IF;
END $$;

-- Проверяем результат
SELECT id, username, role, 
       CASE 
           WHEN password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
           THEN 'Пароль установлен правильно (admin)'
           ELSE 'ВНИМАНИЕ: Пароль отличается от ожидаемого'
       END as password_status
FROM users 
WHERE username = 'admin';
