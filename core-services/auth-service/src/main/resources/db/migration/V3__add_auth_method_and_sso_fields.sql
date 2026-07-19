-- Add auth method and SSO fields to credentials table
ALTER TABLE credentials 
    ADD COLUMN auth_method VARCHAR(20) NOT NULL DEFAULT 'PHONE_OTP',
    ADD COLUMN email VARCHAR(255) UNIQUE,
    ADD COLUMN entra_object_id VARCHAR(255) UNIQUE;

-- Make phone and password_hash nullable for SSO-only credentials
ALTER TABLE credentials ALTER COLUMN phone DROP NOT NULL;
ALTER TABLE credentials ALTER COLUMN password_hash DROP NOT NULL;

-- Create unique indexes for performance and constraint integrity
CREATE UNIQUE INDEX idx_credentials_entra_oid ON credentials(entra_object_id) WHERE entra_object_id IS NOT NULL;
CREATE UNIQUE INDEX idx_credentials_email ON credentials(email) WHERE email IS NOT NULL;
