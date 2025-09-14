-- V6__sync_schema.sql (예시)
-- cohort_member.joined_at
ALTER TABLE cohort_member
  ADD COLUMN IF NOT EXISTS joined_at timestamp NOT NULL DEFAULT now();
ALTER TABLE cohort_member
  ALTER COLUMN joined_at DROP DEFAULT;

-- 혹시 추후 또 필요한 컬럼이 있다면 여기에 계속 추가
-- 예) ALTER TABLE some_table ADD COLUMN IF NOT EXISTS created_at timestamp NOT NULL DEFAULT now();
--     ALTER TABLE some_table ALTER COLUMN created_at DROP DEFAULT;
