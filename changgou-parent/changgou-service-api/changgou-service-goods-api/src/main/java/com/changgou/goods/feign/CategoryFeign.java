package com.changgou.goods.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Category;

/**
 * @author caijia
 * @Date 2020年12月24日 16:50:00
 */
@FeignClient(name = "goods")
public interface CategoryFeign{

    @GetMapping("/category/{id}")
    Result<Category> findById(@PathVariable("id") Integer id);

}
