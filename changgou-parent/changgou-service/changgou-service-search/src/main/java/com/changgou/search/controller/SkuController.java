package com.changgou.search.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.changgou.entity.Result;
import com.changgou.search.service.SkuService;

/**
 * controller
 * 用于接收页面传递的请求 来测试 导入数据
 * 实现搜索的功能
 *
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.search.controller *
 * @since 1.0
 */
@RestController
@CrossOrigin
@RequestMapping("/search")
public class SkuController {

    @Autowired
    private SkuService skuService;

    @RequestMapping("/import")
    public Result importData() {

        skuService.importData();
        return new Result(true, entity.StatusCode.OK, "导入成功");
    }

    /**
     *
     * @param searchMap  搜索的条件 map
     * @return  resultMap  返回的结果 map
     */
    @GetMapping
    public Map<String,Object> search(@RequestParam(required = false) Map<String,String> searchMap){
       return  skuService.search(searchMap);
    }
}
