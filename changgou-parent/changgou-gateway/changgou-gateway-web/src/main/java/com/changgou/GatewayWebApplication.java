package com.changgou;

import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

import reactor.core.publisher.Mono;

/**
 * @author caijia
 * @Date 2021年01月20日 17:42:00
 */
@SpringBootApplication
@EnableEurekaClient
public class GatewayWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayWebApplication.class,args);
    }

    //创建一个ipKeyResolver 指定用户的IP
    // @Bean(name="ipKeyResolver")
    // public KeyResolver keyResolver(){
    //     return new KeyResolver() {
    //         @Override
    //         public Mono<String> resolve(ServerWebExchange exchange) {
    //             //1.获取请求request对象
    //             ServerHttpRequest request = exchange.getRequest();
    //             //2.从request中获取ip地址
    //             String hostString = request.getRemoteAddress().getHostString();//Ip地址
    //
    //             //3.返回
    //             return Mono.just(hostString);
    //         }
    //     };
    // }

    /**
     * 创建用户唯一标识 使用IP作为用户唯一标识 来根据IP进行限流操作
     */
    @Bean(name = "ipKeyResolver")
    public KeyResolver userKeyResolver(){
        return exchange -> {
            String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostString();
            return Mono.just(ip);
        };
    }
}
