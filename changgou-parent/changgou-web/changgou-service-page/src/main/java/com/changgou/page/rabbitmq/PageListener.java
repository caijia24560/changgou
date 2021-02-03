package com.changgou.page.rabbitmq;

import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.changgou.page.service.PageService;

/**
 * 监听队列,获取消息,并生成静态页面
 * @author caijia
 * @Date 2020年12月25日 09:28:00
 */
@Component
@RabbitListener(
        bindings = @QueueBinding(value = @Queue(value = RabbitMqConfig.PAGE_CREATE_QUEUE),
                            exchange = @Exchange(value = RabbitMqConfig.GOODS_UP_EXCHANGE)
        ))
public class PageListener{

    @Autowired
    private PageService pageService;

    // @RabbitListener(queues = RabbitMqConfig.PAGE_CREATE_QUEUE)
    // public void receiveMessage(String spuId){
    //     System.out.println("生成商品详情页,商品id:"+spuId);
    //     pageService.generateItempage(spuId);
    // }

    @RabbitHandler
    public void receiveMessage(String spuId){
        System.out.println("生成商品详情页,商品id:"+spuId);
        pageService.generateItempage(spuId);
    }

}
