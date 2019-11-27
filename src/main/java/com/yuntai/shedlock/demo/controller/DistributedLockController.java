package com.yuntai.shedlock.demo.controller;

import com.yuntai.shedlock.demo.util.DistributedLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/lock")
@Slf4j
public class DistributedLockController {

    @Autowired
    RedissonClient redissonClient;

    @GetMapping("/task")
    public void task(){
        log.info("task start");
        RLock lock = redissonClient.getLock("LOCK:1001");
        boolean getLock = false;

        try {
            if (getLock = lock.tryLock(0,5, TimeUnit.SECONDS)){
                //执行业务逻辑
                System.out.println("拿到锁干活");

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
        log.info("testUtil start");
        boolean getLock = false;

        try {
            if (getLock = DistributedLockUtil.tryLock("LOCK:1001",TimeUnit.SECONDS,0,5)){
                //执行业务逻辑
                System.out.println("拿到锁干活");

            }else {
                log.info("Redisson分布式锁没有获得锁:{},ThreadName:{}","LOCK:1001",Thread.currentThread().getName());
            }

        } catch (Exception e) {
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

}
