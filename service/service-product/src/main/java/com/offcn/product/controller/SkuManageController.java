package com.offcn.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offcn.common.result.Result;
import com.offcn.model.product.SkuInfo;
import com.offcn.model.product.SpuImage;
import com.offcn.model.product.SpuSaleAttr;
import com.offcn.product.service.ManageService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
@RequestMapping("/admin/product")
public class SkuManageController {
    @Autowired
    private ManageService manageService;
    /**
     * 根据spuId 查询spuImageList
     * @param spuId
     * @return
     */
    @RequestMapping("/spuImageList/{spuId}")
    public Result<List<SpuImage>> spuImageList(@PathVariable("spuId") Long spuId){
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }
    /**
     * 根据spuId 查询销售属性集合
     * @param spuId
     * @return
     */
    @RequestMapping("/spuSaleAttrList/{spuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable("spuId") Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }
    /**
     * 保存sku
     * @param skuInfo
     * @return
     */
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        // 调用服务层
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }
    @RequestMapping("/list/{pageNum}/{pageSize}")
    public Result<IPage<SkuInfo>> page(@PathVariable int pageNum,@PathVariable int pageSize){
        Page<SkuInfo>  p = new Page<>(pageNum,pageSize);
        IPage<SkuInfo> page = manageService.getPage(p);
        return Result.ok(page);
    }
    @RequestMapping("/onSale/{skuId}")
    public void onSale(@PathVariable Long skuId){
        manageService.onSale(skuId);
    }
    @RequestMapping("/cancelSale/{skuId}")
    public void cancelSale(@PathVariable Long skuId){
        manageService.cancelSale(skuId);
    }
}
