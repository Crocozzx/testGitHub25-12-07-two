package com.offcn.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.offcn.model.product.BaseTrademark;

public interface BaseTrademarkService extends IService<BaseTrademark> {
    //查询所有的品牌，并且分页
    IPage<BaseTrademark> getPage(Page<BaseTrademark> pageParam);
}
