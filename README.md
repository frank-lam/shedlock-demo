# 基于shedLock，解决分布式定时任务
## 步骤一：引入依赖
> shedLock本质基于锁实现的分布式定时调度的控制，且shedLock支持基于多种技术实现的锁。这里基于redis实现。
```
<dependency>
   <groupId>net.javacrumbs.shedlock</groupId>
   <artifactId>shedlock-spring</artifactId>
   <version>2.3.0</version>
</dependency>
<dependency>
   <groupId>net.javacrumbs.shedlock</groupId>
   <artifactId>shedlock-provider-redis-spring</artifactId>
   <version>2.3.0</version>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
   <dependency>
   <groupId>org.apache.commons</groupId>
   <artifactId>commons-pool2</artifactId>
</dependency>
```
## 步骤二：application.yml中redis配置
```
spring:
  profiles:
    active: dev
  redis:
    host: 127.0.0.1
    port: 6379
    password:
    database: 0
    lettuce:
      pool:
        max-active: 32
        max-wait: 300ms
        max-idle: 16
        min-idle: 8
```
## 步骤三：JavaConfig配置
```
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@Slf4j
public class ShedLockConfig {

    @Value("${spring.profiles.active}")
    private String env;

    @Bean
    public LockProvider lockProvider(@Qualifier("redisConnectionFactory") RedisConnectionFactory connectionFactory) {
        //环境变量 -需要区分不同环境避免冲突，如dev环境和test环境，两者都部署时，只有一个实例进行，此时会造成相关环境未启动情况
        log.info(connectionFactory.toString());
        return new RedisLockProvider(connectionFactory, env);
    }
}
```
## 步骤四：使用
```
@Component
@Slf4j
public class SimpleTask {

    //区分服务
    @Value("${server.port}")
    String port;

    //为了方便测试 设置cron表达式
    @Scheduled(cron = "*/5 * * * * ?")
    @SchedulerLock(name="simpleTask",lockAtLeastFor = 1*1000)
    public void getCurrentDate() {
        log.info("端口({}),Scheduled定时任务执行：{}", port, new Date());
    }
}
```
## 步骤五：Jmeter测试
- 本地启动多个工程
- Jmeter模拟多个并发请求分别同时请求
