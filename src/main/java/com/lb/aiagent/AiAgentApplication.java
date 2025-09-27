package com.lb.aiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@MapperScan("com.lb.aiagent.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class AiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
    }

}
