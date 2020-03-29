# 基于shedLock，解决分布式定时任务
## 步骤一：引入依赖
> shedLock本质基于锁实现的分布式定时调度的控制，且shedLock支持基于多种技术实现的锁。这里基于redis实现。
```
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>4.5.2</version>
</dependency>

<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-redis-spring</artifactId>
    <version>4.6.0</version>
</dependency>
```
## 步骤二：application.yml中redis配置
redis的配置这里使用biz的redis配置
## 步骤三：JavaConfig配置
```
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "30s")
public class ShedLockConfiguration {

    @Bean
    public LockProvider lockProvider(JedisConnectionFactory bizConnectionFactory) {
        return new RedisLockProvider(bizConnectionFactory);
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
