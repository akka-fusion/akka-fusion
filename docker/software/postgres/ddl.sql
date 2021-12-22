ALTER DATABASE template1 SET timezone TO 'Asia/Chongqing';
create user af with nosuperuser replication encrypted password '2021.Af';
-- create database af owner=af template=template1;
create database af_user owner=af template=template1;
create database af_oauth owner=af template=template1;
