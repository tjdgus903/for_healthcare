ALTER TABLE cohort
  ADD COLUMN IF NOT EXISTS org_id UUID;

-- 필요하다면 외래키 제약 조건도 추가
-- ALTER TABLE cohort
--   ADD CONSTRAINT fk_cohort_org
--   FOREIGN KEY (org_id) REFERENCES orgs(id);
