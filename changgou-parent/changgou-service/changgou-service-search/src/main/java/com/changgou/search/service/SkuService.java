package com.changgou.search.service;

import java.util.Map;

/**
 * @author caijia
 * @Date 2020年12月07日 16:16:00
 */
public interface SkuService{

    /**
     * 导入数据到索引库中
     */
    void importData();

    /**
     * 条件搜索
     * @param searchMap
     * @return
     */
    Map<String,Object> search(Map<String,String> searchMap);
}
