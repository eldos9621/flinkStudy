# flinkStudy
Flink学习

per-job模式
.独享dispatcher和resource mannager
.按需申请资源
.适合执行时间长且稳定的大任务
session模式
.共享dispatcher和resource mannager
.共享资源
.适合执行时间段且规模小的任务

Flink中两种基本的调度策略。
Eager
.适用于流作业
Lazy From Source 
.适用于批作业


错误恢复
Restart-all 
.该策略会直接重启所有的 Task
Restart-individual 
.该策略只适用于 Task 之间不需要数据传输的作业，对于这种作业可以只重启出现错误的 Task
Region-based 
.如果是由于下游任务本身导致的错误，可以只重启下游对应的 Region
.如果是由于上游失败导致的错误，那么需要同时重启上游的 Region 和下游的 Region。
.如果下游的输出使用了非确定的数据分割方式，为了保持数据一致性，还需要同时重启所有上游 Region 的下游 Region


时间定义
Processing Time 
.是来模拟我们真实世界的时间
.我们得到的处理结果（或者说流处理应用的内部状态）是不确定的
Event Time 
.是数据世界的时间
.无论重放数据多少次，都能得到一个相对确定可重现的结果


Checkpoint

理解state
什么是keyed state 有两个特点：
.只能应用于 KeyedStream 的函数与操作中，例如 Keyed UDF, windowstate
.keyed state 是已经分区 / 划分好的，每一个 key 只能属于某一个 keyedstate
什么是operator state
.又称为 non-keyed state，每一个operatorstate都仅与一个operator的实例绑定。
.常见的 operator state 是 source state，例如记录当前 source 的 offset

Checkpoint 执行机制详解
.第一步，Checkpoint Coordinator 向所有 source 节点 trigger Checkpoint
.第二步，source 节点向下游广播 barrier，这个 barrier 就是实现 Chandy-Lamport 分布式快照算法的核心，下游的 task 只有收到所有 input 的 barrier 才会执行相应的 Checkpoint
.第三步，当 task 完成 state 备份后，会将备份数据的地址（state handle）通知给 Checkpoint coordinator
.第四步，下游的 sink 节点收集齐上游两个 input 的 barrier 之后，会执行本地快照，然后 Flink 框架会从中选择没有上传的文件进行持久化备份
.第五步 sink 节点在完成自己的 Checkpoint 之后，会将 state handle 返回通知 Coordinator
.最后，当 Checkpoint coordinator 收集齐所有 task 的 state handle，就认为这一次的 Checkpoint 全局完成了，向持久化存储中再备份一个Checkpoint meta 文件



Flink架构

JobManager 的功能主要有：
.将 JobGraph 转换成 Execution Graph，最终将 Execution Graph 拿来运
行；
.Scheduler 组件负责 Task 的调度；
.Checkpoint Coordinator 组 件 负 责 协 调 整 个 任 务 的 Checkpoint， 包 括Checkpoint 的开始和完成；
.通过 Actor System 与 TaskManager 进行通信；
.其它的一些功能，例如 Recovery Metadata，用于进行故障恢复时，可以从Metadata 里面读取数据。


TaskManager 是负责具体任务的执行过程，在 JobManager 申请到资源之后开始启动。TaskManager 里面的主要组件有：
.Memory & I/O Manager，即内存 I/O 的管理；
.Network Manager，用来对网络方面进行管理；
.Actor system，用来负责网络的通信；
.TaskManager 被分成很多个 TaskSlot，每个任务都要运行在一个 TaskSlot里面，TaskSlot 是调度资源里的最小单位。
