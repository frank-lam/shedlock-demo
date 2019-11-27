package com.yuntai.shedlock.demo.controller;

import com.yuntai.shedlock.demo.util.DistributedLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/lock")
@Slf4j
public class DistributedLockController {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/task")
    public void task(){
        log.info("task start");
        RLock lock = redissonClient.getLock("LOCK:1001");
        boolean getLock = false;

        try {
            if (getLock = lock.tryLock(0,5, TimeUnit.SECONDS)){
                //执行业务逻辑
                doSomething();

            }else {
                log.info("Redisson分布式锁没有获得锁:{},ThreadName:{}","LOCK:1001",Thread.currentThread().getName());
            }

        } catch (InterruptedException e) {
            log.error("Redisson 获取分布式锁异常,异常信息:{}",e);
        }finally {
            if (!getLock){
                return;
            }
            //如果演示的话需要注释该代码;实际应该放开
            //lock.unlock();
            //log.info("Redisson分布式锁释放锁:{},ThreadName :{}", KeyConst.REDIS_LOCK_KEY, Thread.currentThread().getName());
        }
    }

    @GetMapping("/testUtil")
    public void testUtil(){

        DistributedLockUtil.lock("LOCK:1001", TimeUnit.SECONDS,5);
        try {
            log.info("Get Lock>>>>>>>>>>>>>>>>>>>");
            doSomething();
        } catch (Exception e) {
            log.error("Redisson 获取分布式锁异常,异常信息:{}",e);
        }finally {
            //如果演示的话需要注释该代码;实际应该放开
            DistributedLockUtil.unlock("LOCK:1001");
            log.info("Redisson分布式锁释放锁:{},ThreadName :{}", "LOCK:1001", Thread.currentThread().getName());
        }
    }

    @GetMapping("/test")
    public void test(){
        doSomething();
    }

    private void doSomething(){
        RedisOperations<String,String> redisOperations = redisTemplate.opsForValue().getOperations();
        String res = redisOperations.boundValueOps("lock:flag").get();
        Integer flag = Integer.parseInt(res);
        flag = flag - 1;

        redisOperations.boundValueOps("lock:flag").set(flag.toString());
        log.info("do something>>>>>>>>>>>>>{}",redisOperations.boundValueOps("lock:flag").get());
    }

}
