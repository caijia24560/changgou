package com.changgou.goods.service;
import java.util.List;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.PageInfo;

/****
 * @Author:admin
 * @Description:Spu业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SpuService {

    void saveGoods(Goods goods);

    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 删除Spu
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
     Spu findById(Long id);

    /***
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();

    /**
     * 添加商品(SPU+ SKUlIST)
     * @param goods   update  add
     */
    void save(Goods goods);

    Goods findGoodsById(Long id);

    void auditSpu(Long id);

    /**
     * 下架
     * @param id
     */
    void pullSpu(Long id);

    void logicDeleteSpu(Long id);


    void restoreSpu(Long id);

    /**
     * 根据spuI查询商品
     * @param spuId
     * @return
     */
    Goods findBySpuId(Integer spuId);

    /**
     * 商品审核
     * @param spuId
     */
    void audit(Integer spuId);
}
