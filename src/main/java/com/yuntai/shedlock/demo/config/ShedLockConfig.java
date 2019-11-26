package com.yuntai.shedlock.demo.config;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * @program: shedlock-demo
 * @description: ShedLockConfig
 * @author: yang Qiankun
 * @create: 2019-11-25 23:51
 **/
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@Slf4j
public class ShedLockConfig {

    @Value("${spring.profiles.active}")
    private String env;

    @Bean
    public LockProvider lockProvider(@Qualifier("redisConnectionFactory") RedisConnectionFactory connectionFactory) {
        //环境变量 -需要区分不同环境避免冲突，如dev环境和test环境，两者都部署时，只有一个实例进行，此时会造成相关环境未启动情况
        log.info(connectionFactory.toString());
        return new RedisLockProvider(connectionFactory, env);
    }
}
