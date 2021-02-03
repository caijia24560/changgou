package com.changgou.goods.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Sku;

/**
 * @author caijia
 * @Date 2020年12月07日 16:05:00
 */
@FeignClient(value = "goods")
public interface SkuFeign{

    /***
     * 查询Sku全部数据
     * @return
     */
    @GetMapping("/sku/all")
    Result<List<Sku>> findAll();

    @GetMapping("/sku/by-spuId/{spuId}")
    Result<List<Sku>> findBySpuId(@PathVariable("spuId") Long spuId);
}
