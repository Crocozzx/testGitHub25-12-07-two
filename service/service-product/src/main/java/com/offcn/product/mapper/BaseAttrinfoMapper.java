package com.offcn.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.offcn.model.product.BaseAttrInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BaseAttrinfoMapper extends BaseMapper<BaseAttrInfo> {
    List<BaseAttrInfo> getAttrValueList(Long category1Id, Long category2Id, Long category3Id);

    //根据skuid查询基本属性
    List<BaseAttrInfo> getAttrList(Long skuId);

}
