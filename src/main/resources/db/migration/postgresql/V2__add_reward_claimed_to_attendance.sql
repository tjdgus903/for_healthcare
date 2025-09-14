ALTER TABLE attendance
  ADD COLUMN IF NOT EXISTS reward_claimed boolean NOT NULL DEFAULT false;