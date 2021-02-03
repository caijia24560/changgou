package com.changgou.goods.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Spu;

/**
 * @author caijia
 * @Date 2020年12月24日 16:56:00
 */
@FeignClient(name = "goods")
public interface SpuFeign{

    @GetMapping("/spu/{id}")
    Result<Spu> findById(@PathVariable("id") Long id);
}
