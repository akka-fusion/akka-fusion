FROM postgres:11
MAINTAINER Yang Jing <yangbajing@gmail.com>

#ENV POSTGIS_MAJOR 2.5
#ENV POSTGIS_VERSION 2.5.2+dfsg-1~exp1.pgdg90+1
#ENV POSTGRES_PASSWORD Postgres.2019
ENV LANG zh_CN.utf8
ENV TZ Asia/Shanghai

RUN localedef -i zh_CN -c -f UTF-8 -A /usr/share/locale/locale.alias zh_CN.UTF-8

COPY ./docs/sql/scheduler.sql /docker-entrypoint-initdb.d/
