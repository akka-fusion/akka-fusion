# Fusion Scheduler Server Fusion调用（作业）服务

## install postgres: 

`docker build . -t fusion-postgres`

```
docker run --name fusion_scheduler -p 5432:5432 \
  -e POSTGRES_USER=devuser \
  -e POSTGRES_PASSWORD=devPass.2019 \
  -e POSTGRES_DB=fusion_scheduler \
  -e POSTGRES_INITDB_ARGS="--encoding=UTF8 --lc-collate=zh_CN.utf8 --lc-ctype=zh_CN.utf8" \
  -d fusion-postgres
```

2. `docker run -it -rm `
