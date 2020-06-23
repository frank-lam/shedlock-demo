package com.yuntai.shedlock.demo.lock;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

public interface DistributedLocker {

    void lock(String lockKey);

    void lock(String lockKey, long timeout);

    void lock(String lockKey, long timeout, TimeUnit unit);

    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit);

    void unlock(String lockKey);

    void unlock(RLock lock);

}
