package com.yuntai.shedlock.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @program: shedlock-demo
 * @description:
 * @author: yang Qiankun
 * @create: 2019-11-26 00:20
 **/
@SpringBootApplication
@EnableScheduling // 开启定时任务
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
    }
}
