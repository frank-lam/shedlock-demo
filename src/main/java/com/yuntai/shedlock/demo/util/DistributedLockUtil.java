package com.yuntai.shedlock.demo.util;

import com.yuntai.shedlock.demo.lock.DistributedLocker;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

public class DistributedLockUtil {

    private static DistributedLocker redissLock;

    public void setRedissLock(DistributedLocker redissLock) {
        DistributedLockUtil.redissLock = redissLock;
    }

    public static void setLocker(DistributedLocker locker) {
        redissLock = locker;
    }

    /**
     * 加锁
     *
     * @param lockKey
     * @return
     */
    public static void lock(String lockKey) {
        redissLock.lock(lockKey);
    }

    /**
     * 带超时的锁
     *
     * @param lockKey
     * @param timeout 超时时间   单位：秒
     */
    public static void lock(String lockKey, int timeout) {
        redissLock.lock(lockKey, timeout);
    }

    /**
     * 带超时的锁，可以传入需要的单位
     *
     * @param lockKey 锁名称
     * @param timeout 超时时间
     * @param unit    单位
     */
    public static void lock(String lockKey, long timeout, TimeUnit unit) {
        redissLock.lock(lockKey, timeout, unit);
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey
     * @param waitTime  最多等待时间
     * @param leaseTime 上锁后自动释放锁时间
     * @return
     */
    public static boolean tryLock(String lockKey, int waitTime, int leaseTime) {
        return redissLock.tryLock(lockKey, waitTime, leaseTime, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey
     * @param unit      时间单位
     * @param waitTime  最多等待时间
     * @param leaseTime 上锁后自动释放锁时间
     * @return
     */
    public static boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        return redissLock.tryLock(lockKey, waitTime, leaseTime, unit);
    }

    /**
     * 释放锁
     *
     * @param lockKey
     */
    public static void unlock(String lockKey) {
        redissLock.unlock(lockKey);
    }

    /**
     * 释放锁
     *
     * @param lock
     */
    public static void unlock(RLock lock) {
        redissLock.unlock(lock);
    }

}
