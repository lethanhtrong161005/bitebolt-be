CREATE TABLE credentials (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  phone VARCHAR(15) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP WITH TIME ZONE,
  updated_at TIMESTAMP WITH TIME ZONE,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_credentials_phone ON credentials(phone);
