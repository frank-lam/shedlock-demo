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
### @SchedulerLock属性说明
- lockAtMostFor属性，该属性指定在执行节点死亡时应将锁保留多长时间。这只是一个后备，在正常情况下，任务完成后立即释放锁定。您必须将lockAtMostFor设置为比正常执行时间长得多的值。如果任务花费的时间超过lockAtMostFor，则结果可能是不可预测的（其他进程将很可能持有该锁）。如果未在@SchedulerLock中指定lockAtMostFor，则将使用@EnableSchedulerLock中的默认值。
- lockAtLeastFor属性，该属性指定应保留锁定的最短时间。其主要目的是在任务很短且节点之间的时钟差的情况下，防止从多个节点执行。
## 步骤五：Jmeter测试
- 本地启动多个工程
- Jmeter模拟多个并发请求分别同时请求
