ALTER TABLE payments ADD COLUMN  user_email VARCHAR(255) NOT NULL DEFAULT 'unknown@ecommerce.com';

ALTER TABLE payments ALTER COLUMN user_email DROP DEFAULT ;