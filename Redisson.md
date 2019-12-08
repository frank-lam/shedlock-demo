# Springboot集成Redisson，实现分布式锁
> Redisson的使用方式，这里分为两种。第一种直接使用Redisson暴露的API，第二种则是使用这里封装的工具类（更简洁）。
## 方式一：直接使用Redisson暴露的API
### 步骤一：添加依赖
```
<dependency>
   <groupId>org.redisson</groupId>
   <artifactId>redisson</artifactId>
   <version>3.11.5</version>
</dependency>
```
### 步骤二：使用JavaConfig装配Redisson
  在application.yml资源文件中添加单机或者哨兵模式配置，以下仅为demo，实际配置需结合实际情况来调整各参数
```
# redisson lock
# 单机模式
redisson:
  address: redis://127.0.0.1:6379
  password:
  
#哨兵模式
#redisson
#  master-name: master
#  password :
#  sentinel-addresses : 127.0.0.1:6379,127.0.0.1:6378
```
  RedissonProperties属性装配
```
@Configuration
@ConfigurationProperties(prefix = "redisson")
@Data
public class RedissonProperties {
    private int timeout = 3000;
    private String address;
    private String password;
    private int database = 0;
    private int connectionPoolSize = 64;
    private int connectionMinimumIdleSize=10;
    private int slaveConnectionPoolSize = 250;
    private int masterConnectionPoolSize = 250;
    private String[] sentinelAddresses;
    private String masterName;
}
```
  RedissonAutoConfiguration 自动装配
```
@Configuration
@ConditionalOnClass(Config.class)
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonAutoConfiguration {

    @Autowired
    private RedissonProperties redissonProperties;

    /**
     * 单机模式自动装配
     * @return
     */
    @Bean
    @ConditionalOnProperty(name="redisson.address")
    RedissonClient redissonSingle() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(redissonProperties.getAddress())
                .setTimeout(redissonProperties.getTimeout())
                .setConnectionPoolSize(redissonProperties.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(redissonProperties.getConnectionMinimumIdleSize());

        if(StringUtils.isNotBlank(redissonProperties.getPassword())) {
            serverConfig.setPassword(redissonProperties.getPassword());
        }
        return Redisson.create(config);
    }

    /**
     * 哨兵模式自动装配
     * @return
     */
    /*@Bean
    @ConditionalOnProperty(name="redisson.master-name")
    RedissonClient redissonSentinel() {
        Config config = new Config();
        SentinelServersConfig serverConfig = config.useSentinelServers().addSentinelAddress(redissonProperties.getSentinelAddresses())
                .setMasterName(redissonProperties.getMasterName())
                .setTimeout(redissonProperties.getTimeout())
                .setMasterConnectionPoolSize(redissonProperties.getMasterConnectionPoolSize())
                .setSlaveConnectionPoolSize(redissonProperties.getSlaveConnectionPoolSize());

        if(StringUtils.isNotBlank(redissonProperties.getPassword())) {
            serverConfig.setPassword(redissonProperties.getPassword());
        }
        return Redisson.create(config);
    }*/

    /**
     * 装配locker类，并将实例注入到RedissLockUtil中，这个是为工具类做准备
     * @return
     */
    @Bean
    DistributedLocker distributedLocker(RedissonClient redissonClient) {
        DistributedLocker locker = new RedissonDistributedLocker();
        ((RedissonDistributedLocker) locker).setRedissonClient(redissonClient);
        DistributedLockUtil.setLocker(locker);
        return locker;
    }

}
```
### 步骤三：使用，这里写测试逻辑
> 说明：doSomething();方法是一个计数的测试方法，用于测试是否所有请求都串行执行。在这里，作者在redis中存放一个数据，用于模拟数据库。
```
    @Autowired
    private RedissonClient redissonClient;

    @GetMapping("/redissonApi")
    public void redissonApi() {
        log.info("task start");
        RLock lock = redissonClient.getLock("LOCK:1001");
        try {
            lock.lock(5, TimeUnit.SECONDS);
            log.info("Get Lock>>>>>>>>>>>>>>>>>>>");
            doSomething();
        } catch (Exception e) {
            log.error("Redisson 获取分布式锁异常,异常信息:{}", e);
        } finally {
            lock.unlock();
            log.info("Release Lock>>>>>>>>>>>>>>>>>>>");
        }
    }
```
### 步骤四：使用Jmeter验证
  - 在ida中启动多个工程
  - 在Jmeter中新建线程组，分别同时对两个启动的工程进行访问测试，观察日志打印。
## 方式二：封装工具类实现
> 仅仅是对Redisson原生API的封装，提供了更易于使用的接口
### 步骤一：定义Lock接口
> 锁可以有不同的实现，除了这里的Redisson，还有基于zk、数据库等的实现，故抽象出接口
```
public interface DistributedLocker {
    RLock lock(String lockKey);
    RLock lock(String lockKey, int timeout);
    RLock lock(String lockKey, TimeUnit unit, int timeout);
    boolean tryLock(String lockKey, TimeUnit unit, int waitTime, int leaseTime);
    void unlock(String lockKey);
    void unlock(RLock lock);
}
```
### 步骤二：基于Redisson的实现
```
@Data
public class RedissonDistributedLocker implements DistributedLocker {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        return lock;
    }

    @Override
    public RLock lock(String lockKey, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, TimeUnit.SECONDS);
        return lock;
    }

    @Override
    public RLock lock(String lockKey, TimeUnit unit, int timeout) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(timeout, unit);
        return lock;
    }

    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, int waitTime, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.unlock();
    }

    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }

}
```
### 步骤三：封装工具类
```
public class DistributedLockUtil {

    private static DistributedLocker redissLock;

    public static void setLocker(DistributedLocker locker) {
        redissLock = locker;
    }

    /**
     * 加锁
     * @param lockKey
     * @return
     */
    public static RLock lock(String lockKey) {
        return redissLock.lock(lockKey);
    }

    /**
     * 释放锁
     * @param lockKey
     */
    public static void unlock(String lockKey) {
        redissLock.unlock(lockKey);
    }

    /**
     * 释放锁
     * @param lock
     */
    public static void unlock(RLock lock) {
        redissLock.unlock(lock);
    }

    /**
     * 带超时的锁
     * @param lockKey
     * @param timeout 超时时间   单位：秒
     */
    public static RLock lock(String lockKey, int timeout) {
        return redissLock.lock(lockKey, timeout);
    }

    /**
     * 带超时的锁
     * @param lockKey
     * @param unit 时间单位
     * @param timeout 超时时间
     */
    public static RLock lock(String lockKey, TimeUnit unit , int timeout) {
        return redissLock.lock(lockKey, unit, timeout);
    }

    /**
     * 尝试获取锁
     * @param lockKey
     * @param waitTime 最多等待时间
     * @param leaseTime 上锁后自动释放锁时间
     * @return
     */
    public static boolean tryLock(String lockKey, int waitTime, int leaseTime) {
        return redissLock.tryLock(lockKey, TimeUnit.SECONDS, waitTime, leaseTime);
    }

    /**
     * 尝试获取锁
     * @param lockKey
     * @param unit 时间单位
     * @param waitTime 最多等待时间
     * @param leaseTime 上锁后自动释放锁时间
     * @return
     */
    public static boolean tryLock(String lockKey, TimeUnit unit, int waitTime, int leaseTime) {
        return redissLock.tryLock(lockKey, unit, waitTime, leaseTime);
    }

}
```
### 步骤四：基于工具类的测试
```
@GetMapping("/testUtil")
    public void testUtil() {

        DistributedLockUtil.lock("LOCK:1001", TimeUnit.SECONDS, 5);
        try {
            log.info("Get Lock>>>>>>>>>>>>>>>>>>>");
            doSomething();
        } catch (Exception e) {
            log.error("Redisson 获取分布式锁异常,异常信息:{}", e);
        } finally {
            //如果演示的话需要注释该代码;实际应该放开
            DistributedLockUtil.unlock("LOCK:1001");
            log.info("Redisson分布式锁释放锁:{},ThreadName :{}", "LOCK:1001", Thread.currentThread().getName());
        }
    }
```
## 总结
根据业务选择合适的分布式锁，Redisson更适用于对数据一致性要求不是很苛刻的场景中。
## 引用
[https://github.com/redisson/redisson/wiki/2.-%E9%85%8D%E7%BD%AE%E6%96%B9%E6%B3%95](https://github.com/redisson/redisson/wiki/2.-%E9%85%8D%E7%BD%AE%E6%96%B9%E6%B3%95)  
[https://juejin.im/post/5da16f0ee51d4578331cbd2d](https://juejin.im/post/5da16f0ee51d4578331cbd2d)  
[https://my.oschina.net/u/3959468/blog/2251918](https://my.oschina.net/u/3959468/blog/2251918)
