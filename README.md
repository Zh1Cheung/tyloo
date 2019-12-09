# Tyloo：Distributed transaction framework——TCC

![tyloo](https://github.com/Zh1Cheung/tyloo/image/tyloo.jpg)



## TCC原理

在 TCC 里，一个业务活动可以有多个事务，每个业务操作归属于不同的事务，即一个事务可以包含多个业务操作。tyloo将每个业务操作抽象成事务参与者，每个事务可以包含多个参与者。

参与者需要声明 try / confirm / cancel 三个类型的方法，和 TCC 的操作一一对应。在程序里，通过 @Compensable 注解标记在 try 方法上，并填写对应的 confirm / cancel 方法，

tyloo有两个拦截器**，**通过对 @Compensable AOP 切面( 参与者 try 方法 )进行拦截，透明化对参与者 confirm / cancel 方法调用，从而实现 TCC。

第一个拦截器，可补偿事务拦截器，实现如下功能：

- 在 Try 阶段，对事务的发起、传播。
- 在 Confirm / Cancel 阶段，对事务提交或回滚。
- 为什么会有对事务的传播呢？在远程调用服务的参与者时，会通过"参数"( 需要序列化 )的形式传递事务给远程参与者。

第二个拦截器，资源协调者拦截器，实现如下功能：

- 在 Try 阶段，添加参与者到事务中。当事务上下文不存在时，进行创建。





## 项目结构

- tcc-transaction-api
  这个模块放的是一些相关的API
- tcc-transaction-core
  主要存放一些事务核心的类，比如两个AoP的拦截器，Repository库等
- tcc-transaction-spring
  Spring框架和事务通过继承整合在这里
- tcc-transaction-tutorial-sample
  业务模块
- tcc-transaction-unit-test
  测试以及工具模块





## 项目展示



![1](https://github.com/Zh1Cheung/tyloo/image/1.jpg)





## 业务流程



![2](https://github.com/Zh1Cheung/tyloo/image/2.jpg)





## **下单支付流程**ppp

j


![3](https://github.com/Zh1Cheung/tyloo/image/3.jpg)





## 配置tcc-transaction

1 引用tcc-transaction

在服务调用方和提供方项目中需要引用tcc-transaction-spring jar包，如使用maven依赖：

```
    <dependency>
        <groupId>io.tyloo</groupId>
        <artifactId>tcc-transaction-spring</artifactId>
        <version>${project.version}</version>
    </dependency>
```

2 加载tcc-transaction.xml配置

启动应用时，需要将tcc-transaction-spring jar中的tcc-transaction.xml加入到classpath中。如在web.xml中配置：

```
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:tcc-transaction.xml
    </param-value>
</context-param>
```

3 设置TransactionRepository

需要为参与事务的应用项目配置一个TransactionRepository，tcc-transaction框架使用transactionRepository持久化事务日志。可以选择FileSystemTransactionRepository、SpringJdbcTransactionRepository、RedisTransactionRepository或ZooKeeperTransactionRepository。

使用SpringJdbcTransactionRepository配置示例如下：

```xml
<bean id="transactionRepository"
      class="SpringJdbcTransactionRepository">
    <property name="dataSource" ref="dataSource"/>
</bean>

<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource"
      destroy-method="close">
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://127.0.0.1:3306/test"/>
    <property name="username" value="root"/>
    <property name="password" value=""/>
</bean>
```

特别提示，dataSource需要单独配置，不能和业务里使用的dataSource复用,即使使用的是同一个数据库。

使用RedisTransactionRepository配置示例如下(需配置redis服务器为AOF模式并在redis.conf中设置appendfsync为always以防止日志丢失)：

```xml
<bean id="transactionRepository" class="RedisTransactionRepository">
<property name="keyPrefix" value="tcc_ut_"/>
<property name="jedisPool" ref="jedisPool"/>
</bean>

<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
<property name="maxTotal" value="1000"/>
<property name="maxWaitMillis" value="1000"/>
</bean>

<bean id="jedisPool" class="redis.clients.jedis.JedisPool">
<constructor-arg index="0" ref="jedisPoolConfig"/>
<constructor-arg index="1" value="127.0.0.1"/>
<constructor-arg index="2" value="6379" type="int"/>
<constructor-arg index="3" value="1000" type="int"/>
<!--<constructor-arg index="4" value="${redis.password}"/>-->
</bean>
```

使用ZooKeeperTransactionRepository配置示例如下：

```xml
<bean id="transactionRepository"
class="ZooKeeperTransactionRepository">
<!--<property name="zkServers" value="localhost:2181,localhost:2183,localhost:2185"/>-->
<property name="zkServers" value="localhost:2181"/>
<property name="zkTimeout" value="10000"/>
<property name="zkRootPath" value="/tcc_ut"/>
</bean>
```

使用FileSystemTransactionRepository配置如下（FileSystemTransactionRepository仅适用事务发布方或调用方应用节点为单节点场景，因为日志是存储在应用节点本地文件中）：

```xml
<bean id="transactionRepository" class="FileSystemTransactionRepository">
<property name="rootPath" value="/data/tcc"/>
</bean>
```

4 设置恢复策略(可选）

当Tcc事务异常后，恢复Job将会定期恢复事务。在Spring配置文件中配置RecoverConfig类型的Bean来设置恢复策略示例：

```xml
<bean class="DefaultRecoverConfig">
    <property name="maxRetryCount" value="30"/>
    <property name="recoverDuration" value="120"/>
    <property name="cronExpression" value="0 */1 * * * ?"/>
    <property name="delayCancelExceptions">
        <util:set>
            <value>com.alibaba.dubbo.remoting.TimeoutException</value>
        </util:set>
    </property>
</bean>
```





## 发布Tcc服务

发布一个Tcc服务方法，可被远程调用并参与到Tcc事务中，发布Tcc服务方法有下面四个约束：

1. 在服务提供方的实现方法上加上@Compensable注解，并设置注解的属性
2. 服务方法第一个入参类型为TransactionContext
3. 服务方法的入参能被序列化(默认使用jdk序列化机制，需要参数实现Serializable接口，可以设置repository的serializer属性自定义序列化实现)
4. try方法、confirm方法和cancel方法入参类型须一样

## 在tcc-transaction-http-capital中发布Tcc服务示例：

try接口方法：

```java
 public String record(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto);
```

try实现方法：

```java
@Compensable(confirmMethod = "confirmRecord", cancelMethod = "cancelRecord", transactionContextEditor = MethodTransactionContextEditor.class)
public String record(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto) {
```

confirm方法：

```java
public void confirmRecord(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto) {
```

cancel方法：

```java
public void cancelRecord(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto) {
```

### 在tcc-transaction-http-redpacket中发布Tcc服务示例：

try接口方法：

```java
public String record(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto);
```

try实现方法：

```java
@Compensable(confirmMethod = "confirmRecord", cancelMethod = "cancelRecord", transactionContextEditor = MethodTransactionContextEditor.class)
public String record(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto) {
```

confirm方法：

```java
public void confirmRecord(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto) {
```

cancel方法：

```java
public void cancelRecord(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto) {
```



## 调用远程Tcc服务

调用远程Tcc服务，将远程Tcc服务参与到本地Tcc事务中，本地的服务方法也需要声明为Tcc服务，与发布一个Tcc服务不同，本地Tcc服务方法有三个约束：

1. 在服务方法上加上@Compensable注解,并设置注解属性
2. 服务方法的入参都须能序列化(实现Serializable接口)
3. try方法、confirm方法和cancel方法入参类型须一样

与发布Tcc服务不同的是本地Tcc服务Compensable注解属性transactionContextEditor可以不用设置。

本地服务通过远程tcc服务提供的client来调用，需要将这些tcc服务的client声明为可加入到TCC事务中，如tcc服务的client方法明为tryXXX,则需要在方法tryXXX上加上如下配置：

```java
@Compensable(propagation = Propagation.SUPPORTS, confirmMethod = "tryXXX", cancelMethod = "tryXXX", transactionContextEditor = MethodTransactionContextEditor.class)
```

其中propagation = Propagation.SUPPORTS表示该方法支持参与到TCC事务中。 如果tcc服务的client为框架自动生成实现（比如代理机制实现）不能添加注解，可为该client实现一个代理类，在代理类的方法上加上注解。

### 在tcc-transaction-http-order中调用远程Tcc服务示例：

try方法：

```java
@Compensable(confirmMethod = "confirmMakePayment",cancelMethod = "cancelMakePayment")
public void makePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {

    System.out.println("order try make payment called");
    order.pay(redPacketPayAmount, capitalPayAmount);
    orderRepository.updateOrder(order);
    capitalTradeOrderService.record(null, buildCapitalTradeOrderDto(order));
    redPacketTradeOrderService.record(null, buildRedPacketTradeOrderDto(order));
}
```

confirm方法：

```java
public void confirmMakePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {
```

cancel方法：

```java
public void cancelMakePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {
```

tcc服务提供的client方法增加注解：

```java
@Compensable(propagation = Propagation.SUPPORTS, confirmMethod = "record", cancelMethod = "record", transactionContextEditor = MethodTransactionContextEditor.class)
public String record(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto) {
```



## rpc框架为dubbo时以隐式传参方式配置

rpc框架为dubbo时支持以隐式传参方式配置TCC事务。



## 配置tcc-transaction

与上面配置tcc-transaction一样，此外，还需要如下步骤：

1. 引用tcc-transaction-dubbo 在项目中需要引用tcc-transaction-dubbo jar包，如使用maven依赖：

   ```xml
    <dependency>
        <groupId>io.tyloo</groupId>
        <artifactId>tcc-transaction-dubbo</artifactId>
        <version>${project.version}</version>
    </dependency>
   ```

2. 加载tcc-transaction-dubbo.xml配置

**需要将tcc-transaction-dubbo jar中的tcc-transaction-dubbo.xml加入到classpath中。**如在web.xml中配置改为：

```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:tcc-transaction.xml,classpath:tcc-transaction-dubbo.xml
    </param-value>
 </context-param>
```



## 发布Tcc服务

发布一个Tcc服务方法，可被远程调用并参与到Tcc事务中，发布支持隐式传参的Tcc服务方法有下面四个约束：

1. 在服务提供方的实现方法上加上@Compensable注解，并设置注解的属性
2. 在服务提供方的接口方法上加上@Compensable注解
3. 服务方法的入参能被序列化(默认使用jdk序列化机制，需要参数实现Serializable接口，可以设置repository的serializer属性自定义序列化实现)
4. try方法、confirm方法和cancel方法入参类型须一样

Compensable的属性包括propagation、confirmMethod、cancelMethod、transactionContextEditor。propagation可不用设置，框架使用缺省值；设置confirmMethod指定CONFIRM阶段的调用方法；设置cancelMethod指定CANCEL阶段的调用方法；设置transactionContextEditor为DubboTransactionContextEditor.class。

### 在tcc-transaction-dubbo-capital中发布Tcc服务示例：

try接口方法：

```java
 @Compensable
public String record(CapitalTradeOrderDto tradeOrderDto);
```

try实现方法：

```java
@Compensable(confirmMethod = "confirmRecord", cancelMethod = "cancelRecord", transactionContextEditor = DubboTransactionContextEditor.class)
public String record(CapitalTradeOrderDto tradeOrderDto) {
```

confirm方法：

```java
public void confirmRecord(CapitalTradeOrderDto tradeOrderDto) {
```

cancel方法：

```java
public void cancelRecord(CapitalTradeOrderDto tradeOrderDto) {
```

## 在tcc-transaction-dubbo-redpacket中发布Tcc服务示例：

try接口方法：

```java
@Compensable
public String record(RedPacketTradeOrderDto tradeOrderDto);
```

try实现方法：

```java
@Compensable(confirmMethod = "confirmRecord", cancelMethod = "cancelRecord", transactionContextEditor = DubboTransactionContextEditor.class)
public String record(RedPacketTradeOrderDto tradeOrderDto) {
```

confirm方法：

```java
public void confirmRecord(RedPacketTradeOrderDto tradeOrderDto) {
```

cancel方法：

```java
public void cancelRecord(RedPacketTradeOrderDto tradeOrderDto) {
```



## 调用远程Tcc服务

调用远程Tcc服务，将远程Tcc服务参与到本地Tcc事务中，本地的服务方法也需要声明为Tcc服务，声明方式与非隐式传参方式一样，有三个约束：

1. 在服务方法上加上@Compensable注解,并设置注解属性
2. 服务方法的入参都须能序列化(实现Serializable接口)
3. try方法、confirm方法和cancel方法入参类型须一样

本地服务通过远程tcc服务提供的client来调用，与非隐式传参方式不一样，无需要将这些tcc服务的client显示地声明为可加入到TCC事务中。



### 在tcc-transaction-dubbo-order中调用远程Tcc服务示例：

try方法：

```java
@Compensable(confirmMethod = "confirmMakePayment", cancelMethod = "cancelMakePayment")
public void makePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {
    System.out.println("order try make payment called.time seq:" + DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));

    order.pay(redPacketPayAmount, capitalPayAmount);
    orderRepository.updateOrder(order);

    String result = capitalTradeOrderService.record(buildCapitalTradeOrderDto(order));
    String result2 = redPacketTradeOrderService.record(buildRedPacketTradeOrderDto(order));
}
```

confirm方法：

```java
public void confirmMakePayment(Order order, BigDecimal redPacketPayAmount, BigDecimal capitalPayAmount) {
```

cancel方法：

```java
public void cancelMakePayment(Order order, BigDecimal redP
```



## 事务与参与者

在 TCC 里，**一个**事务( `Transaction` ) 可以有**多个**参与者( `Participant` )参与业务活动

本质上，TCC 通过多个参与者的 try / confirm / cancel 方法，实现事务的最终一致性**。





## 事务管理器

TransactionManager，事务管理器，提供事务的获取、发起、提交、回滚，参与者的新增等等方法。

- begin()方法——发起根事务（该方法在调用方法类型为 MethodType.ROOT 、在事务处于 Try 阶段被调用）
- propagationNewBegin(...)方法——传播发起分支事务（该方法在调用方法类型为 MethodType.PROVIDER 、在事务处于 Try 阶段被调用）
- propagationExistBegin(...)方法——传播获取分支事务（在调用方法类型为 MethodType.PROVIDER、在事务处于 Confirm / Cancel 阶段被调用）
- commit(...)方法——提交事务（该方法在事务处于 Confirm / Cancel 阶段被调用）
- rollback(...)方法—— 回滚事务（该方法在事务处于 Confirm / Cancel 阶段被调用）
- enlistParticipant(...) 方法——添加参与者到事务（该方法在事务处于 Try 阶段被调用）



## 事务拦截器

 基于@Compensable和@Aspect 注解 AOP 切面实现业务方法的 TCC 事务声明拦截，同 Spring 的 org.springframework.transaction.annotation.@Transactional 的实现。

- CompensableTransactionInterceptor，可补偿事务拦截器。
- ResourceCoordinatorInterceptor，资源协调者拦截器。
- XXXInterceptor通过 `org.aspectj.lang.annotation.@Pointcut` + `org.aspectj.lang.annotation.@Around` 注解，配置对 **@Compensable 注解的方法**进行拦截，调用 `CompensableTransactionInterceptor#interceptXXXMethod(...)` 方法进行处理。
