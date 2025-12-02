package com.offcn.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.ListFeignClient;
import com.offcn.item.service.ItemService;
import com.offcn.model.product.BaseCategoryView;
import com.offcn.model.product.SkuInfo;
import com.offcn.model.product.SpuSaleAttr;
import com.offcn.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@SuppressWarnings({"all"})
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private ListFeignClient listFeignClient;

    @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        Map<String, Object> result = new HashMap<>();
        //引入线程池之后，要使用线程池，否则以下函数自己开启的线程效率太低
        CompletableFuture<SkuInfo> skuInfo1 = CompletableFuture.supplyAsync(() -> {
            // 通过skuId 查询skuInfo
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            // 保存skuInfo
            result.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);
        CompletableFuture<Void> spuSaleAttrList1 = skuInfo1.thenAcceptAsync(skuInfo -> {
            // 销售属性-销售属性值回显并锁定
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
            // 保存数据
            result.put("spuSaleAttrList", spuSaleAttrList);
        }, threadPoolExecutor);

        CompletableFuture<Void> voidCompletableFuture = skuInfo1.thenAcceptAsync(skuInfo -> {
            //根据spuId 查询map 集合属性
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            // 保存 json字符串
            String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
            // 保存valuesSkuJson
            result.put("valuesSkuJson",valuesSkuJson);
        }, threadPoolExecutor);

        CompletableFuture<Void> price = skuInfo1.thenAcceptAsync(skuInfo -> {
            //获取商品最新价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);//这个其实也不用非得依赖skuinfo的返回值，
            // 然后在非得等skuinfo完了之后在执行，但是为了2345是一个整体，所以在这依赖了
            // 获取价格
            result.put("price", skuPrice);
        }, threadPoolExecutor);
        CompletableFuture<Void> categoryView1 = skuInfo1.thenAcceptAsync(skuInfo -> {
            //获取商品分类
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            //保存商品分类数据
            result.put("categoryView", categoryView);
        }, threadPoolExecutor);
        //更新商品incrHotScore
        CompletableFuture<Void> incrHotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        }, threadPoolExecutor);

        //阻塞，skuInfo1加不加进来无所谓，因为其他四个线程都是在skuInfo1之后执行的。所以无所谓
        skuInfo1.allOf(skuInfo1,spuSaleAttrList1,voidCompletableFuture,price,categoryView1,
                incrHotScoreCompletableFuture).join();
        //.join();表示将这几个方法最后的结果合并。
        return result;
    }
}
