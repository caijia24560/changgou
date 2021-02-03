package com.changgou.page.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.page.service.PageService;

/**
 * @author caijia
 * @Date 2020年12月24日 17:00:00
 */
@Service
public class PageServiceImpl implements PageService{

    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private CategoryFeign categoryFeign;
    @Autowired
    private SpuFeign spuFeign;
    @Value("${pagepath}")
    private String pagepath;
    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 生成静态化页面id
     * @param spuId
     */
    @Override
    public void generateItempage(String spuId){
        Context context = new Context();
        Map<String, Object> map = this.findItemData(spuId);
        context.setVariables(map);
        //获取商品详情页生成的指定位置
        File dir = new File(pagepath);
        //不存在就创建文件夹
        if(!dir.exists()){
            dir.mkdirs();
        }
        //定义输出流,进行文件生成
        File file = new File(dir + "/" + spuId + ".html");
        PrintWriter writer = null;
        try{
            writer = new PrintWriter(file);
            //生成文件
            /**
             * 参数1: 模板名称
             * 2: context对象,所需数据
             * 3: 输出流
             */
            templateEngine.process("item", context, writer);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }finally{
            //关闭流
            Objects.requireNonNull(writer).close();
        }

    }

    /**
     * 获取静态化页面数据
     * @param spuId
     * @return
     */
    public Map<String, Object> findItemData(String spuId){
        HashMap<String, Object> resultMap = new HashMap<>();
        Result<Spu> spuResult = spuFeign.findById(Long.valueOf(spuId));
        Spu spu = spuResult.getData();
        if(spu == null){
            throw new RuntimeException("商品不存在");
        }
        //获取图片信息
        if(StringUtils.isNotBlank(spu.getImages())){
            resultMap.put("imageList", spu.getImages().split(","));
        }
        //获取分类信息 总共3级
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        resultMap.put("category1", category1);
        Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
        resultMap.put("category2", category2);
        Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();
        resultMap.put("category3", category3);
        resultMap.put("spu", spu);
        //获取所有sku集合
        Result<List<Sku>> bySpuId = skuFeign.findBySpuId(Long.valueOf(spuId));
        resultMap.put("skuList", bySpuId.getData());
        //获取所有规格数据
        resultMap.put("specificationList", JSON.parseObject(spu.getSpecItems(), Map.class));

        return resultMap;

    }
}
