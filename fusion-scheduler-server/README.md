# Fusion Scheduler Server Fusion调度（作业）服务

基于Akka Cluster实现调度服务集群化，使用Akka Cluster Singleton保证同一时间只有一个活动节点处理任务调度管理，并保证主节点挂掉后自动选择新的主节点继续任务调度管理。

## Run

### install/start postgres: 

```
docker build . -t postgres_fusion-scheduler
```

```
docker run --name fusion_scheduler -p 5432:5432 -d postgres_fusion-scheduler
```

### Start

1. 运行 `fusion.scheduler.SchedulerApplication` 启动服务
2. 执行 `fusion.scheduler.grpc.GrpcClientTest` 单元测试，使用GRPC客户端访问调度（作业）服务
