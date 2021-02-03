package com.changgou.search.controller;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.changgou.entity.Page;
import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;

/**
 * 描述
 *
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.search.controller *
 * @since 1.0
 */
@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String,String> searchMap, Model model){
        Map<String, Object> returnMap = skuFeign.search(searchMap);
        model.addAttribute("result", returnMap);
        //分页查询
        Page<SkuInfo> pageInfo = new Page<>(Long.parseLong(returnMap.get("total").toString()),
                Integer.parseInt(returnMap.get("pageNumber").toString())+1,
                Integer.parseInt(returnMap.get("pageSize").toString()));

        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("searchMap", searchMap);
        String[] genUrl = genUrl(searchMap);
        model.addAttribute("url", genUrl[0]);
        model.addAttribute("sortUrl", genUrl[1]);

        return "search";
    }

    /**
     * 拼接用户请求url
     * @return
     */
    public String[] genUrl(Map<String, String> searchMap){
        String url = "/search/list";  //默认地址
        String sortUrl = "/search/list";  //排序地址
        if(!CollectionUtils.isEmpty(searchMap)){
            url += "?";
            sortUrl += "?";
            for(Entry<String, String> entry : searchMap.entrySet()){
                url += entry.getKey()+"="+entry.getValue()+"&";
                //跳过分页参数
                if(entry.getKey().equalsIgnoreCase("pageNum")){
                    continue;
                }

                if(entry.getKey().equalsIgnoreCase("sortField") ||
                   entry.getKey().equalsIgnoreCase("sortRule")){
                    continue;
                }
                sortUrl += entry.getKey()+"="+entry.getValue()+"&";
            }
            url = url.substring(0, url.length()-1);
            sortUrl = sortUrl.substring(0, sortUrl.length()-1);
        }
        return new String[]{url,sortUrl};
    }
}
