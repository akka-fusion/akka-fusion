-- af_user

set timezone to 'Asia/Chongqing';
create schema af_user;

create table af_user."user"
(
    id          bigint,
    name        varchar,
    age         int         not null default 0,
    sex         int         not null default 0,
    permissions int[]       not null default '{}',
    avatar_url  varchar,
    status      int         not null default 1,
    create_time timestamptz not null,
    update_time timestamptz not null default to_timestamp(0),
    deleted     int         not null default 0,
    constraint user_pk primary key (id)
);

create table af_user.user_credential
(
    id            bigserial,
    enterprise_id bigint,
    phone         varchar,
    email         varchar,
    username      varchar,
    salt_password varchar,
    password      varchar,
    create_time   timestamptz not null,
    update_time   timestamptz not null default to_timestamp(0),
    deleted       int         not null default 0,
    constraint user_credential_pk primary key (id)
);
create unique index user_credential_uidx_phone on af_user.user_credential (phone);
create unique index user_credential_uidx_email on af_user.user_credential (email);
create unique index user_credential_uidx_username on af_user.user_credential (username);

-- 用户设备
create table af_user.user_device
(
    user_id     bigint,
    app_id      int         not null,
    platform    int         not null,
    extras      jsonb       not null,
    create_time timestamptz not null,
    update_time timestamptz not null default to_timestamp(0),
    constraint user_device_pk primary key (user_id)
);

-- af_user.user <-> mdm.stall
create table af_user.user_stall
(
    user_id     bigint      not null,
    stall_id    bigint      not null,
    create_time timestamptz not null default now(),
    constraint user_stall_pk primary key (user_id, stall_id)
);

-- 收货地址
create table af_user.receiving_address
(
    id          bigserial,
    user_id     bigint      not null,
    area_id     bigint      not null default 0,
    address     varchar     not null,
    contact     varchar     not null,
    phone       varchar     not null,
    status      int         not null default 1,
    first       int2        not null default 0,
    deleted     int2        not null default 0,
    create_time timestamptz not null,
    update_time timestamptz not null default to_timestamp(0),
    constraint receiving_address_kf_user foreign key (user_id) references af_user."user",
    constraint receiving_address_pk primary key (id)
);
