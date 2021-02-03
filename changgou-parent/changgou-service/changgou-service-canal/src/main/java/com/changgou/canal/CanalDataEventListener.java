package com.changgou.canal;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.changgou.canal.config.RabbitMqConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.DeleteListenPoint;
import com.xpand.starter.canal.annotation.InsertListenPoint;
import com.xpand.starter.canal.annotation.ListenPoint;

/**
 * @author caijia
 * @Date 2020年12月01日 16:56:00
 */
@CanalEventListener
public class CanalDataEventListener{

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * @InsertListenPoint 新增数据监听
     * @param eventType 监听类型
     * @param rowData 修改的行数据
     */
    @InsertListenPoint
    public void onEnventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        for(CanalEntry.Column column : rowData.getAfterColumnsList()){
            System.out.println("修改数据:"+column.getName()+"----"+column.getValue());
        }
    }

    /**
     * @InsertListenPoint 删除数据监听
     * @param eventType 监听类型
     * @param rowData 修改的行数据
     */
    @DeleteListenPoint
    public void onEnventDelete(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        for(CanalEntry.Column column : rowData.getBeforeColumnsList()){
            System.out.println("删除前数据:"+column.getName()+"----"+column.getValue());
        }
        for(CanalEntry.Column column : rowData.getAfterColumnsList()){
            System.out.println("删除后数据:"+column.getName()+"----"+column.getValue());
        }
    }

    /**
     * @InsertListenPoint 自定义监听数据监听
     * @param eventType 监听类型
     * @param rowData 修改的行数据
     */
    @ListenPoint(
            eventType = {EventType.DELETE,EventType.UPDATE}, //制定监听类型
            schema = {"changgou_content"},   //指定监听的数据库
            table = {"tb_content"},    //指定监听的表
            destination = "example"
    )
    public void onEnventPoint(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        for(CanalEntry.Column column : rowData.getBeforeColumnsList()){
            System.out.println("自定义监听数据:"+column.getName()+"----"+column.getValue());
        }
    }

    /**
     * @InsertListenPoint 自定义监听商品信息变化
     * @param eventType 监听类型
     * @param rowData 修改的行数据
     */
    @ListenPoint(
            eventType = {EventType.DELETE,EventType.UPDATE}, //制定监听类型
            schema = {"changgou_goods"},   //指定监听的数据库
            table = {"tb_spu"},    //指定监听的表
            destination = "example"
    )
    public void onSpuEnventPoint(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        //判断操作类型
        if (eventType == CanalEntry.EventType.DELETE) {
            String spuId = "";
            List<Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();//spuid
                    break;
                }
            }
            //todo 删除静态页

        } else {
            //新增 或者 更新
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            String spuId = "";
            for (CanalEntry.Column column : afterColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();
                    break;
                }
            }
            //将商品的spuid发送到mq
            rabbitTemplate.convertAndSend(RabbitMqConfig.GOODS_UP_EXCHANGE,"",spuId);
        }
    }

    /**
     * @InsertListenPoint 自定义监听商品信息变化
     * @param eventType 监听类型
     * @param rowData 修改的行数据
     */

    // @ListenPoint(
    //         eventType = {EventType.DELETE,EventType.UPDATE}, //制定监听类型
    //         schema = {"changgou_goods"},   //指定监听的数据库
    //         table = {"tb_spu"},    //指定监听的表
    //         destination = "example"
    // )
    // public void goodsUp(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
    //     //获取改变之前的数据并将这部分数据转换为map
    //     Map<String,String> oldData=new HashMap<>();
    //     rowData.getBeforeColumnsList().forEach((c)->oldData.put(c.getName(),c.getValue()));
    //
    //     //获取改变之后的数据并这部分数据转换为map
    //     Map<String,String> newData = new HashMap<>();
    //     rowData.getAfterColumnsList().forEach((c)->newData.put(c.getName(),c.getValue()));
    //
    //     //获取最新上架的商品 0->1
    //     if ("0".equals(oldData.get("is_marketable")) && "1".equals(newData.get("is_marketable"))){
    //         //将商品的spuid发送到mq
    //         rabbitTemplate.convertAndSend(RabbitMqConfig.GOODS_UP_EXCHANGE,"",newData.get("id"));
    //     }
    //
    //     //获取最新下架的商品 1->0
    //     if ("1".equals(oldData.get("is_marketable")) && "0".equals(newData.get("is_marketable"))){
    //         //将商品的spuid发送到mq
    //         rabbitTemplate.convertAndSend(RabbitMqConfig.GOODS_DOWN_EXCHANGE,"",newData.get("id"));
    //     }
    //
    //     //获取最新被审核通过的商品  status    0->1
    //     if ("0".equals(oldData.get("status")) && "1".equals(newData.get("status"))){
    //         //将商品的spuid发送到mq
    //         rabbitTemplate.convertAndSend(RabbitMqConfig.GOODS_UP_EXCHANGE,"",newData.get("id"));
    //     }
    // }
}
