# 예시 파일
create table if not exists users (
  id uuid primary key,
  email text not null unique,
  password_hash text not null,
  role text not null
);

create table if not exists attendance (
  id uuid primary key,
  user_id uuid not null references users(id) on delete cascade,
  date date not null,
  status text not null,
  reward_claimed boolean not null default false,
  constraint uq_user_date unique (user_id, date)
);
