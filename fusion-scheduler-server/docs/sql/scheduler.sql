create table qrtz_job_details
(
	sched_name varchar(120) not null,
	job_name varchar(200) not null,
	job_group varchar(200) not null,
	description varchar(250),
	job_class_name varchar(250) not null,
	is_durable boolean not null,
	is_nonconcurrent boolean not null,
	is_update_data boolean not null,
	requests_recovery boolean not null,
	job_data bytea,
	created_at timestamp default CURRENT_TIMESTAMP not null,
	constraint qrtz_job_details_pkey
		primary key (sched_name, job_name, job_group)
);

create index idx_qrtz_j_req_recovery
	on qrtz_job_details (sched_name, requests_recovery);

create index idx_qrtz_j_grp
	on qrtz_job_details (sched_name, job_group);

create table qrtz_triggers
(
	sched_name varchar(120) not null,
	trigger_name varchar(200) not null,
	trigger_group varchar(200) not null,
	job_name varchar(200) not null,
	job_group varchar(200) not null,
	description varchar(250),
	next_fire_time bigint,
	prev_fire_time bigint,
	priority integer,
	trigger_state varchar(16) not null,
	trigger_type varchar(8) not null,
	start_time bigint not null,
	end_time bigint,
	calendar_name varchar(200),
	misfire_instr smallint,
	job_data bytea,
	created_at timestamp default CURRENT_TIMESTAMP not null,
	constraint qrtz_triggers_pkey
		primary key (sched_name, trigger_name, trigger_group),
	constraint qrtz_triggers_sched_name_fkey
		foreign key (sched_name, job_name, job_group) references qrtz_job_details
);

create index idx_qrtz_t_j
	on qrtz_triggers (sched_name, job_name, job_group);

create index idx_qrtz_t_jg
	on qrtz_triggers (sched_name, job_group);

create index idx_qrtz_t_c
	on qrtz_triggers (sched_name, calendar_name);

create index idx_qrtz_t_g
	on qrtz_triggers (sched_name, trigger_group);

create index idx_qrtz_t_state
	on qrtz_triggers (sched_name, trigger_state);

create index idx_qrtz_t_n_state
	on qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);

create index idx_qrtz_t_n_g_state
	on qrtz_triggers (sched_name, trigger_group, trigger_state);

create index idx_qrtz_t_next_fire_time
	on qrtz_triggers (sched_name, next_fire_time);

create index idx_qrtz_t_nft_st
	on qrtz_triggers (sched_name, trigger_state, next_fire_time);

create index idx_qrtz_t_nft_misfire
	on qrtz_triggers (sched_name, misfire_instr, next_fire_time);

create index idx_qrtz_t_nft_st_misfire
	on qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);

create index idx_qrtz_t_nft_st_misfire_grp
	on qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);

create table qrtz_simple_triggers
(
	sched_name varchar(120) not null,
	trigger_name varchar(200) not null,
	trigger_group varchar(200) not null,
	repeat_count bigint not null,
	repeat_interval bigint not null,
	times_triggered bigint not null,
	constraint qrtz_simple_triggers_pkey
		primary key (sched_name, trigger_name, trigger_group),
	constraint qrtz_simple_triggers_sched_name_fkey
		foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

create table qrtz_cron_triggers
(
	sched_name varchar(120) not null,
	trigger_name varchar(200) not null,
	trigger_group varchar(200) not null,
	cron_expression varchar(120) not null,
	time_zone_id varchar(80),
	constraint qrtz_cron_triggers_pkey
		primary key (sched_name, trigger_name, trigger_group),
	constraint qrtz_cron_triggers_sched_name_fkey
		foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

create table qrtz_simprop_triggers
(
	sched_name varchar(120) not null,
	trigger_name varchar(200) not null,
	trigger_group varchar(200) not null,
	str_prop_1 varchar(512),
	str_prop_2 varchar(512),
	str_prop_3 varchar(512),
	int_prop_1 integer,
	int_prop_2 integer,
	long_prop_1 bigint,
	long_prop_2 bigint,
	dec_prop_1 numeric(13,4),
	dec_prop_2 numeric(13,4),
	bool_prop_1 boolean,
	bool_prop_2 boolean,
	constraint qrtz_simprop_triggers_pkey
		primary key (sched_name, trigger_name, trigger_group),
	constraint qrtz_simprop_triggers_sched_name_fkey
		foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

create table qrtz_blob_triggers
(
	sched_name varchar(120) not null,
	trigger_name varchar(200) not null,
	trigger_group varchar(200) not null,
	blob_data bytea,
	constraint qrtz_blob_triggers_pkey
		primary key (sched_name, trigger_name, trigger_group),
	constraint qrtz_blob_triggers_sched_name_fkey
		foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers
);

create table qrtz_calendars
(
	sched_name varchar(120) not null,
	calendar_name varchar(200) not null,
	calendar bytea not null,
	constraint qrtz_calendars_pkey
		primary key (sched_name, calendar_name)
);

create table qrtz_paused_trigger_grps
(
	sched_name varchar(120) not null,
	trigger_group varchar(200) not null,
	constraint qrtz_paused_trigger_grps_pkey
		primary key (sched_name, trigger_group)
);

create table qrtz_fired_triggers
(
	sched_name varchar(120) not null,
	entry_id varchar(95) not null,
	trigger_name varchar(200) not null,
	trigger_group varchar(200) not null,
	instance_name varchar(200) not null,
	fired_time bigint not null,
	sched_time bigint not null,
	priority integer not null,
	state varchar(16) not null,
	job_name varchar(200),
	job_group varchar(200),
	is_nonconcurrent boolean,
	requests_recovery boolean,
	constraint qrtz_fired_triggers_pkey
		primary key (sched_name, entry_id)
);

create index idx_qrtz_ft_trig_inst_name
	on qrtz_fired_triggers (sched_name, instance_name);

create index idx_qrtz_ft_inst_job_req_rcvry
	on qrtz_fired_triggers (sched_name, instance_name, requests_recovery);

create index idx_qrtz_ft_j_g
	on qrtz_fired_triggers (sched_name, job_name, job_group);

create index idx_qrtz_ft_jg
	on qrtz_fired_triggers (sched_name, job_group);

create index idx_qrtz_ft_t_g
	on qrtz_fired_triggers (sched_name, trigger_name, trigger_group);

create index idx_qrtz_ft_tg
	on qrtz_fired_triggers (sched_name, trigger_group);

create table qrtz_scheduler_state
(
	sched_name varchar(120) not null,
	instance_name varchar(200) not null,
	last_checkin_time bigint not null,
	checkin_interval bigint not null,
	constraint qrtz_scheduler_state_pkey
		primary key (sched_name, instance_name)
);

create table qrtz_locks
(
	sched_name varchar(120) not null,
	lock_name varchar(40) not null,
	constraint qrtz_locks_pkey
		primary key (sched_name, lock_name)
);
