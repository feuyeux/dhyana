## 选主模块
> dhyana [di'ɑ:nə] 禅
> 在本模块的类名中代指"选主"功能

### 入口类 DhyanaEngine

#### init
1. 构建`DhyanaCandidate`信息，通过`ConfigTransport`注册自己到Redis
2. 通过`ConfigTransport`从Redis读取选主配置信息`DhyanaConfig`，用于选主计算
3. 通过`ConfigTransport`从Redis读取候选人列表`DhyanaCandidate`集合
4. 选主计算
5. 点对点确认，有冲突通知对方重新选举
6. 选主结束

如果超时、选主失败，从第3步开始重试；超过重试次数，则服务应结束，进程退出。

#### keepalive
1. 点对点探测
2. 如果发现是主跪了，则和其他节点确认主跪，确认无主后，注册自己。重复init流程。

#### 集群状态
- 初始化
- 无主|选主中
- 我是主|他是主

#### 线程及回调
- 选主通信
- 心跳通信