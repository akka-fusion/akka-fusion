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

