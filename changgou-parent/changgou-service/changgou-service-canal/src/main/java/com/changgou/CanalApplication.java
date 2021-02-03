package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

import com.xpand.starter.canal.annotation.EnableCanalClient;

/**
 * @author caijia
 * @Date 2020年12月01日 16:55:00
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableCanalClient
@EnableEurekaClient
public class CanalApplication{
    public static void main(String[] args){
        SpringApplication.run(CanalApplication.class, args);
    }

}
