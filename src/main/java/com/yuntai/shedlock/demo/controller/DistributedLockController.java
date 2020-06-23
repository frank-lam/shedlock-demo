package com.yuntai.shedlock.demo.controller;

import com.yuntai.shedlock.demo.util.DistributedLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class DistributedLockController {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     *
     */
    @GetMapping("/lock")
    public void redissonApi() {
        log.info("task start");
        RLock lock = redissonClient.getLock("LOCK:1001");
        try {
            lock.lock(5, TimeUnit.SECONDS);
            doSomething();
        } catch (Exception e) {
            log.error("Redisson 获取分布式锁异常,异常信息:{}", e);
        } finally {
            lock.unlock();
            //log.info("Release Lock>>>>>>>>>>>>>>>>>>>");
        }
    }

    @GetMapping("/lockUtil")
    public void testUtil() throws InterruptedException {
        DistributedLockUtil.lock("LOCK:1001",5);
        try {
            doSomething();
        } catch (Exception e) {
            log.error("Redisson 获取分布式锁异常,异常信息:{}", e);
        } finally {
            DistributedLockUtil.unlock("LOCK:1001");
            //log.info("Redisson分布式锁释放锁:{},ThreadName :{}", "LOCK:1001", Thread.currentThread().getName());
        }
    }

    @GetMapping("/tryLockUtil")
    public void testUtil2() throws InterruptedException {
        try {
            boolean b = DistributedLockUtil.tryLock("LOCK:1001", 1, 5);
            log.info("b = {},current Thread: {}",b,Thread.currentThread().getName());
            if (b) {
                doSomething();
            }
        } catch (Exception e) {
            log.error("Redisson 获取分布式锁异常,异常信息:{}", e);
        } finally {
            DistributedLockUtil.unlock("LOCK:1001");
            //log.info("Redisson分布式锁释放锁:{},ThreadName :{}", "LOCK:1001", Thread.currentThread().getName());
        }
    }

    @GetMapping("/test")
    public void test() throws InterruptedException {
        doSomething();
    }

    private void doSomething() throws InterruptedException {
        Thread.sleep(500);
        RedisOperations<String, String> redisOperations = redisTemplate.opsForValue().getOperations();
        String res = redisOperations.boundValueOps("lock:flag").get();
        Integer flag = Integer.parseInt(res);
        flag = flag - 1;

        redisOperations.boundValueOps("lock:flag").set(flag.toString());
        log.info("do something---------------{}", redisOperations.boundValueOps("lock:flag").get());
    }

}
