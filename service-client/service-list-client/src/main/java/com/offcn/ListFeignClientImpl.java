package com.offcn;

import com.offcn.common.result.Result;
import com.offcn.model.list.SearchParam;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class ListFeignClientImpl implements ListFeignClient {
    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }

    @Override
    public Result list(SearchParam listParam) {
        return null;
    }

    @Override
    public Result upperGoods(Long skuId) {
        return null;
    }

    @Override
    public Result lowerGoods(Long skuId) {
        return null;
    }


}
