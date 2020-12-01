# tyloo
Distributed transaction framework——TCC



## 概念

- Try: 尝试执行业务
  - 完成所有业务检查（一致性）
  - 预留必须业务资源（准隔离性）

- Confirm: 确认执行业务
  - 真正执行业务
  - 不作任何业务检查
  - 只使用Try阶段预留的业务资源
  - Confirm操作满足幂等性 

- Cancel: 取消执行业务

  - 释放Try阶段预留的业务资源
  - Cancel操作满足幂等性

  



## 环境

- Java
- Maven
- Git
- MySQL
- Redis
- Zookeeper
- Intellij IDEA





## 功能

- 基于 Spring AOP 切面思想实现对分布式事务注解的拦截。
- 基于Dubbo的ProxyFactory代理机制为服务接口生成代理对象。
- 基于Mysql、Redis乐观锁进行事务版本控制以及基于基于Quartz进行事务恢复。
- 支持多种事务日志序列化以及事务存储器实现。
- 调用方式（版本）：Dubbo、HTTP





## 业务场景

- https://www.cnblogs.com/jajian/p/10014145.html

- **我们有必要使用TCC分布式事务机制来保证各个服务形成一个整体性的事务**







## 运行

1. 导入数据库脚本`tyloo/tyloo-tutorial-sample/src/tylooSampledb`
2. 修改三个配置文件`tccjdbc.properties`
3. 修改三个子项目（capital、redpacket、order）的启动配置：HTTP port







## 模块

- **两个拦截器**

  - 通过对 @Tyloo AOP 切面( 参与者 try 方法 )进行拦截，透明化对参与者confirm / cancel 方法调用，从而实现 TCC

- **事务与参与者**

  - TCC 通过多个参与者的 try / confirm / cancel 方法，实现事务的最终一致性
  - Tyloo 将每个业务操作抽象成事务参与者

- **事务管理器**

  - 提供事务的获取、发起、提交、回滚，参与者的新增等等方法。

- **事务存储器**

  - 提供对事务对象的持久化

- **事务注解**

  - 传播级别
  - 确认/取消执行业务方法
  - 事务上下文编辑

- **事务恢复**

  - 事务信息被持久化到外部的存储器中。事务存储是事务恢复的基础。通过读取外部存储器中的异常事务，定时任务会按照一定频率对事务进行重试，直到事务完成或超过最大重试次数。

  





## 流程

- 因为对远程业务的调用需要用到代理对象，代理对象由dubbo service生成，TRY阶段在进行远程调用前需要调用代理对象（代理对象的confrimmethod和cancelmethod均和try方法名字相同），此时拦截器会进行拦截代理对象，拦截后调用远程业务，远程业务（远程业务本地有个本地事务保证执行成功）也会被拦截，然后远程业务执行。COMMIT阶段时主业务执行完comfirm方法后代理对象执行comfirm方法（confirm方法均为反射调用），但是代理对象的confirm方法还是代理对象的try方法，此时再被拦截器拦截，因为此时代理对象的事务状态已经改为CONFIRMING，由于事务类型为defalut（因为代理对象的传播级别默认为SUPPORT），所以直接过了。此时再调用远程业务的try方法，走拦截，invoke反射的时候反射远程业务的confrim方法，因为try方法做了幂等所以直接过了，此时远程业务的commit阶段完成，然后继续下一个远程业务。最后根事务提交，完成。















