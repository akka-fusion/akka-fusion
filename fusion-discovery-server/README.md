# Fusion Discovery Server

## 特性

1. 配置服务
2. 名字服务（服务发现）

## 技术

1. 使用Akka GRPC提供RPC API
2. 使用Akka HTTP提供REST API
3. 基于 Akka Cluster 实现服务的集群化
4. 使用 Akka Distributed Data配置和服务状态数据在集群各节点的分发
5. 使用 Akka Persistence持久化配置，服务状态恢复

## Run

### install/start postgres: 

```
cd docs/
docker build . -t postgres_fusion-discovery
```

```
docker run --name fusion_discovery -p 45432:5432 -d postgres_fusion-discovery
```

### install/start cassandra:

```
docker run --name cassandra1 -p 9042:9042 -d cassandra:3.11
```

**启动其它节点**

```
docker run --name cassandra2 -e CASSANDRA_SEEDS="$(docker inspect --format='{{
    .NetworkSettings.IPAddress }}' cassandra1)" -p 9043:9042 -d cassandra:3.11
```

```
docker run --name cassandra3 -e CASSANDRA_SEEDS="$(docker inspect --format='{{
    .NetworkSettings.IPAddress }}' cassandra1)" -p 9044:9042 -d cassandra:3.11
```

