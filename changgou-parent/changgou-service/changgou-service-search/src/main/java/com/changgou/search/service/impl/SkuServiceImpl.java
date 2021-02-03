package com.changgou.search.service.impl;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms.Bucket;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder.Field;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;

import com.changgou.entity.Result;

/**
 * @author caijia
 * @Date 2020年12月07日 16:17:00
 */
@Service
public class SkuServiceImpl implements SkuService{

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    //elasticsearchTemplate实现索引库的CRUD
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public void importData(){
        //1.调用 goods微服务的fegin 查询 符合条件的sku的数据
        Result<List<Sku>> skuResult = skuFeign.findAll();
        List<Sku> data = skuResult.getData();//sku的列表
        List<Sku> skus = data.stream().filter(sku -> sku.getStatus().equals("1")).collect(Collectors.toList());
        //将sku的列表 转换成es中的skuinfo的列表
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(skus), SkuInfo.class);
        for(SkuInfo skuInfo : skuInfos){
            //获取规格的数据  {"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"165"}

            //转成MAP  key: 规格的名称  value:规格的选项的值
            Map<String, Object> map = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(map);
        }
        // 2.调用spring data elasticsearch的API 导入到ES中
        skuEsMapper.saveAll(skuInfos);

    }

    /**
     * 条件搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap){

        handlerSearchMap(searchMap);

        NativeSearchQueryBuilder builder = getNativeSearchQueryBuilder(searchMap);

        Map<String, Object> resultMap = searchList(builder);

        // //判断用户是否选择了分类和品牌,选择后不需要显示此行
        // if(resultMap != null){
        //     //分类分组查询
        //     if(StringUtils.isEmpty(searchMap.get("category"))){
        //         List<String> categoryList = searchCategoeyList(builder);
        //         resultMap.put("categoryList", categoryList);
        //     }
        //     //品牌分类分组查询
        //     if(StringUtils.isEmpty(searchMap.get("brand"))){
        //         List<String> brandList = searchBrandList(builder);
        //         resultMap.put("brandList", brandList);
        //     }
        // }
        // //组装一下规格数据
        // HashMap<String, Set<String>> specList = searchSpecList(builder);
        // resultMap.put("specList", specList);

        //分组搜索实现
        Map<String, Object> searchGroup = searchGroup(builder, searchMap);
        resultMap.putAll(searchGroup);

        return resultMap;
    }

    /**
     * 处理规格有"+"号的搜索条件
     * @param searchMap
     */
    public void handlerSearchMap(Map<String, String> searchMap){
        if(searchMap != null){
            for(Entry<String, String> entry : searchMap.entrySet()){
                if(entry.getKey().startsWith("spec_")){
                    entry.setValue(entry.getValue().replace("+", "%2B"));
                }
            }
        }
    }

    /**
     * 搜索结果集封装
     * @param builder
     * @return
     */
    public Map<String, Object> searchList(NativeSearchQueryBuilder builder){
        //设置高亮显示
        Field field = new Field("name"); //指定高亮域
        //拼html显示颜色
        field.preTags("<em style=\"color:yellow;\">");
        field.postTags("</em>");
        //碎片长度  关键词数据的长度
        field.fragmentSize(100);
        builder.withHighlightFields(field);

        // AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class, new SearchResultMapper(){
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable){
                //储存所有转换后的高亮数据对象
                List<T> list = new ArrayList<>();
                for(SearchHit hit : response.getHits()){
                    //获取非高亮数据
                    SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                    //分析结果集数据,获取高亮数据 -> 只有某个域的高亮数据
                    HighlightField highlightField = hit.getHighlightFields().get("name");
                    if(highlightField != null && highlightField.getFragments() != null){
                        //非高亮数据中指定的域替换成高亮数据
                        Text[] texts = highlightField.getFragments();
                        StringBuilder stringBuilder = new StringBuilder();
                        for(Text text : texts){
                            stringBuilder.append(text.toString());
                        }
                        //非高亮数据中指定的域替换成高亮数据
                        skuInfo.setName(stringBuilder.toString());
                    }
                    list.add((T)skuInfo);
                }
                return new AggregatedPageImpl<T>(list, pageable, response.getHits().getTotalHits());
            }
        });
        //获取搜索封装信息
        NativeSearchQuery query = builder.build();
        Pageable pageable = query.getPageable();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        //分页总记录数
        long totalElements = page.getTotalElements();
        //分页总页数
        int pages = page.getTotalPages();
        //结果集
        List<SkuInfo> list = page.getContent();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", list);
        resultMap.put("total", totalElements);
        resultMap.put("pages", pages);
        resultMap.put("pageSize", pageSize);
        resultMap.put("pageNumber", pageNumber);
        return resultMap;
    }

    /**
     * 搜索条件封装
     * @param searchMap
     * @return
     */
    public NativeSearchQueryBuilder getNativeSearchQueryBuilder(Map<String, String> searchMap){
        //执行搜索, 响应结果
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //添加搜索关键词信息
        if(searchMap != null && !searchMap.isEmpty()){
            String keywords = searchMap.get("keywords");
            String brandName = searchMap.get("brand");
            String categoryName = searchMap.get("category");
            if(StringUtils.isNotEmpty(keywords)){
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }
            if(StringUtils.isNotEmpty(brandName)){
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", brandName));
            }
            if(StringUtils.isNotEmpty(categoryName)){
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", categoryName));
            }
            //规格过滤实现 将参数加一个前缀
            for(Entry<String, String> entry : searchMap.entrySet()){
                if(entry.getKey().startsWith("spec_")){
                    String value = entry.getValue().replace("\\", "");
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap." + entry.getKey().replaceAll("spec_", "") + ".keyword", value));
                }
            }
            //价格区间
            String price = searchMap.get("price");
            if(StringUtils.isNotBlank(price)){
                price = price.replaceAll("元", "").replaceAll("以上", "");
                String[] prices = price.split("-");
                if(StringUtils.isNotBlank(prices[0])){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                }
                if(StringUtils.isNotBlank(prices[1])){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lt(Integer.parseInt(prices[1])));
                }
            }
            //排序实现
            String sortField = searchMap.get("sortField");  //指定排序的域(type)
            String sortRule = searchMap.get("sortRule");  //排序规则
            if(StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortRule)){
                builder.withSort(new FieldSortBuilder(sortField).order(SortOrder.fromString(sortRule)));
            }
        }


        //如果用户不传分页参数 默认第一页
        Integer pageNum = coverPage(searchMap);
        Integer PageSize = 30;
        builder.withPageable(PageRequest.of(pageNum - 1, PageSize));
        builder.withQuery(boolQueryBuilder);
        return builder;
    }

    /**
     * 组装分页参数
     */
    public Integer coverPage(Map<String, String> searchMap){
        if(searchMap != null){
            String pageNum = searchMap.get("pageNum");
            if(StringUtils.isNotBlank(pageNum)){
                return Integer.parseInt(pageNum);
            }
            return 1;
        }
        return 1;
    }

    /**
     * 分组查询分类集合
     * .addAggregation();添加一个聚合操作
     * 取别名
     * 表示根据那个域进行查询
     */
    public List<String> searchCategoeyList(NativeSearchQueryBuilder builder){
        builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //获取分组数据
        StringTerms skuCategory = aggregatedPage.getAggregations().get("skuCategory");
        List<String> categoryList = new ArrayList<>();
        for(Bucket bucket : skuCategory.getBuckets()){
            String categoryName = bucket.getKeyAsString();//其中一个分类名称
            categoryList.add(categoryName);
        }
        return categoryList;
    }

    /**
     * 根据品牌进行分组
     * .addAggregation();添加一个聚合操作
     * 取别名
     * 表示根据那个域进行查询
     */
    public List<String> searchBrandList(NativeSearchQueryBuilder builder){
        builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //获取分组数据
        StringTerms skuBrand = aggregatedPage.getAggregations().get("skuBrand");
        List<String> brandList = new ArrayList<>();
        for(Bucket bucket : skuBrand.getBuckets()){
            String categoryName = bucket.getKeyAsString();//其中一个分类名称
            brandList.add(categoryName);
        }
        return brandList;
    }

    /**
     * 规格分组合并
     * 返回格式 Map<String, List<String>>
     */
    public HashMap<String, Set<String>> searchSpecList(NativeSearchQueryBuilder builder){
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //获取分组数据
        StringTerms skuSpec = aggregatedPage.getAggregations().get("skuSpec");
        List<String> specList = new ArrayList<>();
        for(Bucket bucket : skuSpec.getBuckets()){
            String spec = bucket.getKeyAsString();//其中一个分类名称
            specList.add(spec);
        }
        HashMap<String, Set<String>> setHashMap = new HashMap<>();
        for(String s : specList){
            Map<String, String> map = JSON.parseObject(s, Map.class);
            for(Entry<String, String> entry : map.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();
                //先获取当前规格数据的集合
                Set<String> set = setHashMap.get(key);
                if(set == null){ //表示之前还没有加进去 新new一个
                    set = new HashSet<>();
                }
                set.add(value);
                setHashMap.put(key, set);
            }
        }

        return setHashMap;
    }

    public Map<String, Object> searchGroup(NativeSearchQueryBuilder builder, Map<String, String> searchMap){
        if(searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))){
            builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }
        if(searchMap == null || StringUtils.isEmpty(searchMap.get("category"))){
            builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        // builder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        // builder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //定义一个map 存储所有分组结果
        Map<String, Object> returnMap = new HashMap<>();

        //获取分组数据
        if(searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))){
            StringTerms skuBrand = aggregatedPage.getAggregations().get("skuBrand");
            returnMap.put("brandList", getGroupList(skuBrand));
        }
        if(searchMap == null || StringUtils.isEmpty(searchMap.get("category"))){
            StringTerms skuCategory = aggregatedPage.getAggregations().get("skuCategory");
            returnMap.put("categaryList", getGroupList(skuCategory));
        }
        StringTerms skuSpec = aggregatedPage.getAggregations().get("skuSpec");
        Map<String, Set<String>> stringSetMap = getStringSetMap(skuSpec);
        returnMap.put("specList",stringSetMap);

        return returnMap;
    }

    /**
     * 获取分组集合数据
     * @param stringTerms
     * @return
     */
    public List<String> getGroupList(StringTerms stringTerms){
        List<String> groupList = new ArrayList<>();
        for(Bucket bucket : stringTerms.getBuckets()){
            String categoryName = bucket.getKeyAsString();//其中一个分类名称
            groupList.add(categoryName);
        }
        return groupList;
    }

    private Map<String, Set<String>> getStringSetMap(StringTerms stringTermsSpec) {
        //key :规格的名称
        //value :规格名称对应的选项的多个值集合set
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Set<String> specValues = new HashSet<String>();
        if (stringTermsSpec != null) {
            //1. 获取分组的结果集
            for (StringTerms.Bucket bucket : stringTermsSpec.getBuckets()) {
                //2.去除结果集的每一行数据()   {"手机屏幕尺寸":"5.5寸","网络":"电信4G","颜色":"白","测试":"s11","机身内存":"128G","存储":"16G","像素":"300万像素"}
                String keyAsString = bucket.getKeyAsString();

                //3.转成JSON 对象  map  key :规格的名称  value:规格名对应的选项的单个值
                Map<String, String> map = JSON.parseObject(keyAsString, Map.class);
                for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
                    String key = stringStringEntry.getKey();//规格名称   手机屏幕尺寸
                    String value = stringStringEntry.getValue();//规格的名称对应的单个选项值 5.5寸

                    //先从原来的specMap中 获取 某一个规格名称 对应的规格的选项值集合
                    specValues = specMap.get(key);
                    if (specValues == null) {
                        specValues = new HashSet<>();
                    }
                    specValues.add(value);
                    //4.提取map中的值放入到返回的map中
                    specMap.put(key, specValues);
                }
            }
        }
        return specMap;
    }
}
