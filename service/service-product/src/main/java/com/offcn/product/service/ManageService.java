package com.offcn.product.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offcn.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ManageService {
    List<BaseCategory1> getCategory1();
    List<BaseCategory2> getCategory2(Long category1Id);
    List<BaseCategory3> getCategory3(Long category2Id);
    List<BaseAttrInfo> getBaseAttrInfo(Long category1Id,Long category2Id,Long category3Id);
    void save(BaseAttrInfo baseAttrInfo);
    //根据id查询属性和属性值
    BaseAttrInfo getAttrInfo(Long id);
    //分页查询
    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo);
    //查询所有的销售属性
    List<BaseSaleAttr> getBaseSaleAttrList();
    //添加spu info 商品
    void save(SpuInfo spuInfo);
    //根据spuId 查询spuImageList
    List<SpuImage> getSpuImageList(Long spuId);
    //根据spuId 查询销售属性集合
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);
    /**
     * 保存数据
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    //分页
    IPage<SkuInfo> getPage(Page<SkuInfo> page);

    //根据id上架
    /**
     * 商品上架
     * @param skuId
     */
    void onSale(Long skuId);

    /**
     * 商品下架
     * @param skuId
     */
    void cancelSale(Long skuId);

    //根据id查询skuinfo
    SkuInfo getSkuInfo(Long skuId);
    //根据三级分类id  查询视图-分类
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);
    //查询价格
    BigDecimal getSkuPrice(Long skuId);


    //根据skuId 查询sku销售属性相关信息

    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) ;

    /**
     * 根据spuId 查询map 集合属性
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(Long spuId);


    List<JSONObject> getBaseCategoryList();

    /**
     * 通过品牌 spuId 来查询数据
     * @param spuid
     * @return
     */
    BaseTrademark getTrademarkByTmId(Long spuid);
    //根据skuid查询基本属性
    List<BaseAttrInfo> getAttrList(Long skuId);








}
