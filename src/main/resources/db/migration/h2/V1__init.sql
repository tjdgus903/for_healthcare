-- === Users ===
create table if not exists "user" (
  id uuid primary key,
  email varchar(120) not null unique,
  password_hash varchar(200) not null,
  role varchar(30) not null,
  created_at timestamp not null,
  deleted_at timestamp null
);

-- === Games ===
create table if not exists game (
  id uuid primary key,
  type varchar(40) not null,       -- ex) COLOR_TAP / SEQUENCE_MEMORY / SHAPE_MATCH
  name varchar(100) not null
);

-- === Game Sessions ===
create table if not exists game_session (
  id uuid primary key,
  user_id uuid not null references "user"(id),
  game_id uuid not null references game(id),
  started_at timestamp not null,
  ended_at timestamp null,
  score integer null,
  accuracy double precision null,
  duration_sec bigint null,
  game_meta text null              -- H2: jsonb -> text
);
create index if not exists idx_session_user on game_session(user_id);
create index if not exists idx_session_game on game_session(game_id);
create index if not exists idx_session_started on game_session(started_at);

-- === Session Metrics (optional key/value metrics per session) ===
create table if not exists session_metric (
  id uuid primary key,
  session_id uuid not null references game_session(id) on delete cascade,
  name varchar(50) not null,
  value_double double precision null,
  value_long bigint null,
  value_text varchar(255) null
);
create index if not exists idx_metric_session on session_metric(session_id);

-- === Attendance ===
create table if not exists attendance (
  id uuid primary key,
  user_id uuid not null references "user"(id),
  date date not null,
  created_at timestamp not null
);
create unique index if not exists uq_attendance_user_date on attendance(user_id, date);

-- === Subscriptions ===
create table if not exists subscription (
  id uuid primary key,
  user_id uuid not null references "user"(id),
  platform varchar(20) not null,          -- ANDROID / IOS
  product_id varchar(120) not null,
  purchase_token varchar(200) not null unique,
  status varchar(20) not null,            -- ACTIVE / CANCELED / EXPIRED
  started_at timestamp not null,
  expires_at timestamp null,
  canceled_at timestamp null,
  last_verified_at timestamp null,
  raw_payload text null
);
create index if not exists idx_sub_user on subscription(user_id);
create index if not exists idx_sub_token on subscription(purchase_token);

-- === Organizations (B2B) ===
create table if not exists organization (
  id uuid primary key,
  name varchar(120) not null,
  created_at timestamp not null default current_timestamp  -- 로컬에서 사용 컬럼 보강
);

create table if not exists organization_member (
  id uuid primary key,
  organization_id uuid not null references organization(id),
  user_id uuid not null references "user"(id),
  role varchar(20) not null,              -- ADMIN / VIEWER ...
  joined_at timestamp not null
);
create unique index if not exists uq_org_member on organization_member(organization_id, user_id);

-- === Cohorts (group of users inside org) ===
create table if not exists cohort (
  id uuid primary key,
  organization_id uuid not null references organization(id),
  name varchar(120) not null
);

create table if not exists cohort_member (
  id uuid primary key,
  cohort_id uuid not null references cohort(id),
  user_id uuid not null references "user"(id)
);
create unique index if not exists uq_cohort_member on cohort_member(cohort_id, user_id);

-- === Consents (privacy / terms / data_share etc.) ===
create table if not exists consent (
  id uuid primary key,
  user_id uuid not null references "user"(id),
  doc varchar(50) not null,               -- "privacy" / "terms" / "data_share"
  version varchar(30) not null,
  accepted boolean not null,
  at timestamp not null,
  ip varchar(64) null,
  user_agent varchar(255) null
);
create index if not exists idx_consent_user on consent(user_id);
