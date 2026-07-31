INSERT INTO credentials (
  id, 
  user_id, 
  phone, 
  password_hash, 
  role, 
  status, 
  created_at, 
  updated_at, 
  created_by, 
  updated_by, 
  is_deleted
) VALUES (
  'a5d91cb6-5c54-47b8-b118-8f81e3a1ef58', 
  'e81bb380-4966-4194-a15d-4f1073860bb4', 
  '03560447291', 
  '$2a$10$xPCS4aD2ZBkwCoWCNU80neIwdAQEWtFxviYniXncuUQNxyMCD.xeO', 
  'CUSTOMER', 
  'ACTIVE', 
  NOW(), 
  NOW(), 
  'system', 
  'system', 
  FALSE
) ON CONFLICT (phone) DO NOTHING;
