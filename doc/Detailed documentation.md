## 源码分析

**web.xml**

因为刚刚运行demo的时候，是直接访问暴露的接口，tomcat收到请求后就会找到资源路径像的web.xml,刚刚运行demo的时候，是访问tyloo-dubbo-order模块的，所以先进order模块的web.xml，在这里，发现其引进了一些配置文件

```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath*:config/spring/local/appcontext-*.xml,classpath:tyloo.xml
    </param-value>
</context-param>
```

`<context-param>`其实就相当于全局的`<init-param>`,初始化servlet时可以获取，在这段代码中，appcontext-*.xml是将所有和业务相关的配置比如servler、datasource、dao、dubbo等引入，tyloo.xml是将事务的相关配置引入，这个xml是放在tyloo-spring模块的，也就是这个xml将一些配置类以bean的形式交给Spring管理



**tyloo.xml**

```xml
 <!-- 开启Spring对@AspectJ风格切面的支持(因为下面用到自定义的TCC补偿切面类) -->
    <!-- @Aspect注解不能被Spring自动识别并注册为Bean,因此要通过xml的bean配置,或通过@Tyloo注解标识其为Spring管理Bean -->
    <aop:aspectj-autoproxy proxy-target-class="true"/>

    <bean id="springBeanFactory" class="io.tyloo.tcctransaction.spring.support.SpringBeanFactory"/>

    <!-- TCC事务配置器 -->
    <bean id="transactionConfigurator" class="io.tyloo.tcctransaction.spring.support.SpringTransactionConfigurator"
          init-method="init"/>
    <!-- 可补偿事务拦截器 -->
    <bean id="tylooAspect" class="io.tyloo.tcctransaction.spring.aspect.ConfigurableTransactionAspect"
          init-method="init">
        <property name="transactionConfigurator" ref="transactionConfigurator"/>
    </bean>
    <!-- 资源协调拦截器 -->
    <bean id="tylooCoordinatorAspect" class="io.tyloo.tcctransaction.spring.aspect.ConfigurableCoordinatorAspect"
          init-method="init">
        <property name="transactionConfigurator" ref="transactionConfigurator"/>
    </bean>
    <!-- 启用定时任务注解 -->
    <task:annotation-driven/>

```



**TylooAspect**

```java
    public void setTylooInterceptor(TylooInterceptor tylooInterceptor) {
        this.tylooInterceptor = tylooInterceptor;
    }

    @Pointcut("@annotation(io.tyloo.api.Tyloo)")
    public void tylooService() {

    }

    @Around("tylooService()")
    public Object interceptTylooMethod(ProceedingJoinPoint pjp) throws Throwable {

        return tylooInterceptor.interceptTylooMethod(pjp);
    }
```



**TylooCoordinatorAspect**

```java

    @Pointcut("@annotation(io.tyloo.api.Tyloo)")
    public void transactionContextCall() {

    }

    @Around("transactionContextCall()")
    public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {
        return tylooCoordinatorInterceptor.interceptTransactionContextMethod(pjp);
    }

    public void setTylooCoordinatorInterceptor(TylooCoordinatorInterceptor tylooCoordinatorInterceptor) {
        this.tylooCoordinatorInterceptor = tylooCoordinatorInterceptor;
    }

```







---







**因为修改数据时才有事务发生，所以可以直接从修改的请求开始**



1. 以OrderController开始
   1. 执行 placeOrder方法
      1. 执行makePayment方法支付
         1. 当点进去makePayment时，发现其方法上贴有 @Tyloo 注解，并且指定了确认和取消的方法的方法名
         2. 因为贴有 @Tyloo 注解，所以进行环绕增强，调用**tylooInterceptor**
            1. 发现返回的 **methodType是ROOT**，即当前方法类型处于根环境，接着往下执行 **rootMethodProceed 方法** 
               1. 走到其 begin 方法
                  1. 先**创建一个根环境的事务对象**
                  2. 通过transactionRepository对象调用 create 方法将创建的事务对象保存在本地
         3. pjp.proceed() 方法继续执行这条执行链
            1. 添加根环境参与者，调用**TylooCoordinatorInterceptor**的 interceptTransactionContextMethod 进行方法增强
               1. **methodType为ROOT**，调用 EnlistRootParticipant 方法（**添加根环境参与者**）
               2. 回到makePayment()方法（**执行主服务的业务**）
               3. **创建当前主环境(Order)的其他消费参与者**
                  1. 在执行 record 方法时，依旧调用tylooInterceptor和TylooCoordinatorInterceptor
                     1. 由于此时的capitalTradeOrderService是本地的一个代理类，所以这个 record 方法实际上是本地代理对 象中的一个方法，在这个方法里中才通过dubbo(RPC)调用远程的record业务方法返回结果,真正的业务方法的实 现是在tyloo-dubbo-capital模块内，而刚刚分析到的程序的执行还是在tyloo-dubbo-order模块中的代理对象上，是没有贴上*@Tyloo*注解的
                     2. 此时获得的**methodType为CONSUMER**,执行 **EnlistConsumerParticipant方法**。接着执行往下完成执行链，此时程序便RPC远程调用实现类的 record 方法，接着执行相应的操作
                     3. 在 makePayment 方法中执行完capitalTradeOrderService.record()方法后便创建完了一个capital**消费参与者**先不管其后续操作，那么在执行完redPacketTradeOrderService.record()就会创建完redpacket的**消费参与者**
                     4. **此时程序通过远程调用来到了Capital模块的CapitalTradeOrderServiceImpl实现类调用 record 方法**，因为这是一个请求来到Capital模块，所以在Capital中这是一个新的线程
                        1. 因为贴有@Tyloo注解,会被TylooTransactionAspect切面切入，由上面的流程可以得出经过TylooTransactionAspect切面可以在本地创建一个transaction对象（**在Capital模块创建分支Transaction对象**）
                        2. 此时被TylooCoordinatorAspect切面切到，此时**methodType是PROVIDER**,所以执行 EnlistProviderParticipant 方法在**Captial分支事务**中添加**Capital参与者**
                        3. **至此，已经创建完Capital的分布式事务**
                        4. 同理，Redpacket的分布式事务
            2. 程序回到interceptTransactionContextMethod方法中的最后一行，执行**pjp.proceed(pjp.getArgs())方法**，将参数列表传入往下执行，因为已经没有切面，所以**开始执行record的真实业务方法 **
               1. **执行capital的try操作**
                  1. 因为贴有@Transactional标签，所以由本地事务管理，因为tradeOrderDto和transferFromAccount是同一个数据库 的数据，也就是连接池一样，这里可以保证tradeOrderDto(之前的数据)和transferFromAccount(修改后的数据)一致 的保存在数据库中 执行 *return* *“**success**”* ，一直返回参数，**此时回到makePayment中,继续往下执行** 
               2. **执行Redpacket的Try操作**
                  1. 相同的原理，RedPacket的分支事务也会被创建好，添加好参与者存在数据库中，然后也执行完Try操作，准备好数 据以及保存了原始的数据在相同的数据库中
               3. **执行Confifirm操作**
                  1. 假如一切正常，则会调用当前transactionManager对象的commit方法提交
                  2. 在 commit 方法中不断遍历当前事务对象的参与者，调用参与者的commit方法 最终执行到terminator对象的 invoke 方法
               4. **Order的Confifirm操作**
                  1. 在PaymentServiceImpl的confifirm方法中，修改订单的状态为CONFIRMED，此时order参与者已经提交
               5. **Capital和RedPacket的Confifirm操作**
                  1. 接着执行到根事务中Capital的参与者，此时的target对象CapitalTradeOrderService的动态代理对象，因为之前存在 这个参与者中的方法是 record 方法，所以执行的也是CapitalTradeOrderService动态代理对象的 record 方法。因为此时Transaction对象的 状态值不是*TRYING*,所以此时判断失败继续往下执行，来到Capital模块的真实 record ，因为贴有@Tyloo标签，所以会被第一个切面拦截，执行 providerMethodProceed 方法
                  2. **至此，Capital的confifirm操作执行完成，同理RedPacket的confifirm操作也执行完成**
               6. **最后删除根事务，整个分布式事务结束**



# 最终流程



## 1.先执行所有的TRY

- 1.首先创建在当前环境(主服务 -> Order的PlaceOrderServiceImpl)**创建根分布式事务**
- 2.接着在根分布式事务中**创建ROOT参与者，也就是主服务的参与者**，这里是Order
- 3**.执行主服务的业务**，操作数据库，但未提交，相当于**Try**
- 4.**添加**到根分布式事务中第一个**CONSUMER参与者(Capital)**
- 5.在对应的分布模块中**创建分支分布式事务**，这个事务关联着根分布式事务，接在**在这个事务中添加PROVIDER参与者,也当前服务的提供,这里就是Capital**
- 6**.执行分支服务的业务，相当于Capital的Try操作**
- 7**.同理，在根分布式事务中创建RedPacket参与者**
- 8.在**对于的分布模块创建分支分布式事务，添加参与者**
- 9.RedPacket的**Try操作**

**此时第一部分所有的TRY都完成**
![img](https://img-blog.csdnimg.cn/20190224025658371.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQwMjMzNTAz,size_16,color_FFFFFF,t_70)

## 2.再执行所有的CONFIRM

**TRY阶段完成后，事务就一定会执行，因为try能完成，就证明资源时可操作的**(事实上，在tcc中，try过后，资源已经被操作了)根分布式事务中的所有参与者依次进行提交操作，调用`confirmMakePayment`方法确认事务提交,并删除每个模块中的事务数据库中的事务对象

**注意，makePayment方法业务贴有@Transactional标签，由本地事务控制，也就是Order模块真正确认数据提交是执行完该方法**

## 3.若TRY阶段有一个失败，则执行整体执行回滚，并利用定时器一直定时观察保持数据最终一致

​		





---







# 异常



## 1.如果TRY的阶段出现了异常



此时错误会被抛回到TylooAspect的 returnValue = pjp.proceed(); 位置，此时后面的catch捕获到,执行 roolback()方法



在 rollback 方法中，类似 commit 方法，拿到当前事务的所有参与者，调用所有参与者 cancelMethod 方法，程序 运行到参与者的根分布式事务环境时，也会去遍历本地参与者执行cancleMethod方法，最后回到根事务，删除根事务，数据回滚完毕



## 2.如果是COMMIT阶段出现了异常

只要TRY通过了，那么数据一定可以操作，所以就一定会执行confifirm，所以confifirm不会出现操作数据库异常，那么 这里的异常就有可能是服务器宕机，此时有对应的定时器来管理，confifirm方法要实现幂等



## 3.如果rollback异常



有对应的定时机制处理



## 定时任务

定时器的3要素: 

1. job：具体需要定时执行的方法

2. trigger：触发点（与job一一对应,定时时间，间隔在此配置） 

3. schedule：调度器（可以调度多个trigger,包括不同类的trigger） 



定义自己的定时任务RecoverScheduledJob 首先初始化定时器，将定时调用的方法，定时任务的名称等(通过 API的方式) 

* 事务恢复定时任务
* 基于 Quartz 实现调度，不断执行事务恢复



Recovery对象中定时执行的方法 startRecover 在Recovery类中编写，主要是将超过设置存活时间的transaction对象从数据库查出,3台服务器都会去执行这个定时任务，所以会**根据根事务还是分支事务做一个过滤**，(分支事务通过判断的时间是超时时间120乘上重试次数30) **判断事务状态是否为 CONFIRMING**，如果是CONFIRMING则会调用transaction.commit()去完成commit方法，解决先持久化到数据库了，到了下一个服务confifirm中宕机或回传异常的问题

> 判断事务状态是否为CANCELLING或者事务类型为ROOT，CANCELLING主要是解决解决先持久化到数据库了，到了 下一个服务rollback中宕机或回传异常的问题，而ROOT则是因为如果根事务数据库中还存在ROOT类型的事务的话， 就一定是异常，将这个分布式事务全部回滚