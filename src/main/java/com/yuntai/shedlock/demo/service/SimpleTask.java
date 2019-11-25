package com.yuntai.shedlock.demo.service;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @program: shedlock-demo
 * @description: simple
 * @author: yang Qiankun
 * @create: 2019-11-26 00:13
 **/
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
