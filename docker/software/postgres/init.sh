#!/bin/sh

psql -U postgres -d template1 -f /data/init.sql
psql -U postgres -d postgres -f /data/ddl.sql

psql -U af -d af_oauth -f /data/af_oauth.sql
psql -U af -d af_user -f /data/af_user.sql
