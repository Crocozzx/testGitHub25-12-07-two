package com.offcn.item.service;

import java.util.Map;

/*public interface ItemService {
    //老电商使用的是 spuid
    //现在该电商使用的是skuid
    *//** 根据skuid查询
     * 需要做
     * 1、sku_info
     * 2\图片
     * 5价格
     * 4\面包屑

     * 3属性属性值
     *这些数据都使用Map<String,Object>来封装
     * 实体类封装不了
     * *//*

    Map<String,Object> getBySkuId(Long skuId);

}*/
public interface ItemService {

    /**
     * 获取sku详情信息
     * @param skuId
     * @return
     */
    Map<String, Object> getBySkuId(Long skuId);
}
