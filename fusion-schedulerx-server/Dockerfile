FROM postgres:12
MAINTAINER Yang Jing <yangbajing@gmail.com>

ENV POSTGRES_USER devuser
ENV POSTGRES_PASSWORD devPass.2019
ENV POSTGRES_DB fusion_scheduler
ENV LANG zh_CN.utf8
ENV TZ Asia/Shanghai

RUN localedef -i zh_CN -c -f UTF-8 -A /usr/share/locale/locale.alias zh_CN.UTF-8

COPY ./docs/sql/scheduler.sql /docker-entrypoint-initdb.d/
