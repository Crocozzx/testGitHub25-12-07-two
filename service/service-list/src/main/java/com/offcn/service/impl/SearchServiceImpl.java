package com.offcn.service.impl;

import com.alibaba.fastjson.JSON;

import com.offcn.mapper.GoodsRepository;
import com.offcn.model.list.*;
import com.offcn.model.product.*;
import com.offcn.product.client.ProductFeignClient;
import com.offcn.service.SearchService;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.management.Query;
import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"all"})
public class SearchServiceImpl implements SearchService {
    @Autowired
    private ProductFeignClient productFeignClient;


    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 上架商品列表
     * @param skuId
     */
    /*@Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //查询sku对应的平台属性
        List<BaseAttrInfo> baseAttrInfoList =  productFeignClient.getAttrList(skuId);
        if(null != baseAttrInfoList) {
            List<SearchAttr> searchAttrList =  baseAttrInfoList.stream().map(baseAttrInfo -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                //一个sku只对应一个属性值
                List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
                searchAttr.setAttrValue(baseAttrValueList.get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());

            goods.setAttrs(searchAttrList);
        }

        //查询sku信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 查询品牌
        BaseTrademark baseTrademark = productFeignClient.getTrademark(skuInfo.getTmId());
        if (baseTrademark != null){
            goods.setTmId(skuInfo.getTmId());
            goods.setTmName(baseTrademark.getTmName());
            goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        }
        // 查询分类
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        if (baseCategoryView != null) {
            goods.setCategory1Id(baseCategoryView.getCategory1Id());
            goods.setCategory1Name(baseCategoryView.getCategory1Name());
            goods.setCategory2Id(baseCategoryView.getCategory2Id());
            goods.setCategory2Name(baseCategoryView.getCategory2Name());
            goods.setCategory3Id(baseCategoryView.getCategory3Id());
            goods.setCategory3Name(baseCategoryView.getCategory3Name());
        }

        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setId(skuInfo.getId());
        goods.setTitle(skuInfo.getSkuName());
        goods.setCreateTime(new Date());

        this.goodsRepository.save(goods);
    }*/

    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();

        // 查询sku 对应平台属性
        // 查mysql  feign调用 product-service
        List<BaseAttrInfo> baseAttrInfoList = productFeignClient.getAttrList(skuId);
        // 把属性往model里倒腾  因为BaseAttrInfo在model里有另外一个属性 叫searchAttr 先封装到searchAttr
        // goods 和 searchAttr是一对多的关系
        if (baseAttrInfoList != null) {

/*
        // 创建空list
        List<SearchAttr> searchAttrList = new ArrayList<>();
        //遍历   SearchAttr  list.add
        for (BaseAttrInfo baseAttrInfo : baseAttrInfoList) {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            searchAttrList.add(searchAttr);
        }
    */

            // 遍历 封装
            List<SearchAttr> searchAttrList = baseAttrInfoList.stream().map(baseAttrInfo -> {
                // 封装
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                // 一个sku只对应一个属性值
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                return searchAttr;

            }).collect(Collectors.toList());

            goods.setAttrs(searchAttrList);

        }

        //查询sku信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        // 封装品牌
        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        if (trademark != null) {
            goods.setTmId(trademark.getId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());
        }
        // 封装三级风类
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        if (baseCategoryView != null) {
            goods.setCategory1Id(baseCategoryView.getCategory1Id());
            goods.setCategory1Name(baseCategoryView.getCategory1Name());
            goods.setCategory2Id(baseCategoryView.getCategory2Id());
            goods.setCategory2Name(baseCategoryView.getCategory2Name());
            goods.setCategory3Id(baseCategoryView.getCategory3Id());
            goods.setCategory3Name(baseCategoryView.getCategory3Name());
        }
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setId(skuInfo.getId());
        goods.setTitle(skuInfo.getSkuName());
        goods.setCreateTime(new Date());

// 某一个skuId 需要上架的信息  利用product  从mysql中 查出来


//  goodsRepository es
        this.goodsRepository.save(goods);
    }

    /**
     * 下架商品列表
     * @param skuId
     */
    @Override
    public void lowerGoods(Long skuId) {
        this.goodsRepository.deleteById(skuId);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void incrHotScore(Long skuId) {
        // 定义key
        String hotKey = "hotScore";
        // 保存数据 增加key对应的集合中元素v1的score值，并返回增加后的值
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        if (hotScore%10==0){
            // 更新es
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(Math.round(hotScore));
            goodsRepository.save(goods);
        }
    }

    @Override
    public SearchResponseVo search(SearchParam searchParam) throws Exception {
        //构建条件，请求三要素

        //发请求得响应
        SearchRequest request = buildQueryDsl(searchParam);
        SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        //处理结果集
        SearchResponseVo searchResponseVo = parseSearchResult(search);
        //实体类中没有的
        searchResponseVo.setPageNo(searchParam.getPageNo());
        searchResponseVo.setPageSize(searchParam.getPageSize());
        long totalPages = (searchResponseVo.getTotal()+searchParam.getPageSize()-1)/searchParam.getPageSize();
        searchResponseVo.setTotalPages(totalPages);
        return searchResponseVo;
    }
    /*private SearchRequest buildQueryDsl(SearchParam searchParam) {

        SearchRequest request = new SearchRequest("goods");

        SearchSourceBuilder sourceBuilders = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //搜索框 判断 关键字内容是否为空
        if (!Strings.isEmpty(searchParam.getKeyword())){
            //根据dsl语句，这个分词查询在这个bool里面，所以放入bool当中
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND);
            boolQueryBuilder.must(title);
        }
        //设置品牌   trademark=2:华为
        if (searchParam.getTrademark()!=null){
            String[] split = searchParam.getTrademark().split(":");
            if (split!=null && split.length==2){//设置条件
                TermQueryBuilder tmId = QueryBuilders.termQuery("tmId", split[0]);
                //这个和上面的分词都是同属于一个boolquery的
                boolQueryBuilder.filter(tmId);
            }
        }
        //判断分类是否为空，空不进行条件 根据dsl语句得知，分类也是在bool中和品牌同一级 也是trem
        if (searchParam.getCategory1Id()!=null){
            TermQueryBuilder category1Id = QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id());
            boolQueryBuilder.filter(category1Id);
        }
        if (searchParam.getCategory2Id()!=null){
            TermQueryBuilder category2Id = QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id());
            boolQueryBuilder.filter(category2Id);
        }
        if (searchParam.getCategory3Id()!=null){
            TermQueryBuilder category3Id = QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id());
            boolQueryBuilder.filter(category3Id);
        }
        //设置属性   属性id:属性值:属性名
        String[] props = searchParam.getProps();//[1:8G:运行内存，1:晓龙8：cpu型号]
        if (props!=null&&props.length>0){
            for (String prop:props){
                String[] split = prop.split(":");
                if (split !=null&& split.length==3){//为什么要=3?因为我们提交的属性结构就是3  属性id:属性值:属性名
                    //提交的这些属性在bool中的bool中
                    BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();
                    //创建带附属条件sub 条件
                    BoolQueryBuilder subboolQuery = QueryBuilders.boolQuery();
                    subboolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    subboolQuery.must(QueryBuilders.termQuery("attrs.AttrValue",split[1]));
                    boolQueryBuilder1.must(QueryBuilders.nestedQuery("attrs",subboolQuery, ScoreMode.None));
                    boolQueryBuilder.filter(boolQueryBuilder1);
                }
            }
        }
        //排序   字段:序
        String order = searchParam.getOrder();
        if (!Strings.isEmpty(order)){
            String field = "hotScore";//如果没传入字段，默认按照分数
            String[] split = order.split(":");
            if (split!=null&&split.length == 2){
                switch (split[0]){//确定排序是哪个字段
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                //根据dsl sort排序是直接在source里面的和最顶层的bool同级
                sourceBuilders.sort(field,"asc".equals(split[1])? SortOrder.ASC:SortOrder.DESC);
            }
        }
        //高亮条件  三要素
        //根据del语句  高亮也是和bool同级
        HighlightBuilder highlight = new HighlightBuilder();
        highlight.field("title");
        highlight.preTags("<span style = color:red>");
        highlight.postTags("</span>");
        sourceBuilders.highlighter(highlight);
        //设置聚合 品牌  有附属条件
        TermsAggregationBuilder brandTermsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")//设置附属条件
            .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmNae"))//继续设置附属条件
            .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));

        sourceBuilders.aggregation(brandTermsAggregationBuilder);
//设置属性  附属条件
        //根据dsl得知，属性中是，分组，在这个分组里面有分为了2个组
        NestedAggregationBuilder attrTermsAggregationBuilder = AggregationBuilders.nested("attrAgg", "attrs");
        AggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")//设置附属条件
                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));
        attrTermsAggregationBuilder.subAggregation(attrIdAgg);

        sourceBuilders.aggregation(attrTermsAggregationBuilder);

        //要从es中查询的字段
        sourceBuilders.fetchSource(new String[]{"id","defaultImg","title","price"},null);

        //设置分页
        sourceBuilders.from(searchParam.getPageNo());
        sourceBuilders.size(searchParam.getPageSize());
        request.source(sourceBuilders);
        return request;
    }*///制作DSL语句
    public SearchRequest buildQueryDsl(SearchParam searchParam){
        //构建查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建多条件查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 判断查询条件是否为空 关键字
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND);
            //关联查询条件到多条件查询对象
            boolQueryBuilder.must(title);
        }
        //判断品牌查询条件是否为空
        String trademark = searchParam.getTrademark();
        if(!StringUtils.isEmpty(trademark)){
            //切开品牌查询条件 trademark=2:华为
            String[] split = trademark.split(":");
            if(split!=null&&split.length==2){
                //创建按照品牌编号进行筛选
                TermQueryBuilder tmId = QueryBuilders.termQuery("tmId", split[0]);
                //关联查询条件到多条件查询对象
                boolQueryBuilder.filter(tmId);
            }

        }
        //构建分类查询条件
        if(null!=searchParam.getCategory1Id()){
            TermQueryBuilder category1Id = QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id());
            boolQueryBuilder.filter(category1Id);
        }
        if(null!=searchParam.getCategory2Id()){
            TermQueryBuilder category2Id = QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id());
            boolQueryBuilder.filter(category2Id);
        }
        if(null!=searchParam.getCategory3Id()){
            TermQueryBuilder category3Id = QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id());
            boolQueryBuilder.filter(category3Id);
        }
        // 构建平台属性查询
        // 23:4G:运行内存
        String[] props = searchParam.getProps();
        if(props!=null&&props.length>0){
            for (String prop : props) {
                //切开
                String[] split = prop.split(":");
                if(split!=null&&split.length==3){
                    //构建嵌套查询对象
                    BoolQueryBuilder boolQuery =  QueryBuilders.boolQuery();
                    //构建嵌套查询子查询对象
                    BoolQueryBuilder subboolQuery=   QueryBuilders.boolQuery();
                    //构建子查询中的过滤条件
                    subboolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    subboolQuery.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    //把子查询合并到嵌套查询对象,设置查询模式nested
                    boolQuery.must(QueryBuilders.nestedQuery("attrs",subboolQuery, ScoreMode.None));
                    //管理嵌套查询到多条件查询
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }

        // 关联多条件查询器对象，到主查询器对象
        searchSourceBuilder.query(boolQueryBuilder);
        //计算分页起始页
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        //设置分页参数
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

        //排序
        String order = searchParam.getOrder();
        if(!StringUtils.isEmpty(order)){
            //切开排序条件
            String[] split = order.split(":");
            if(split!=null&&split.length==2){
                //排序字段
                String field=null;
                switch (split[0]){
                    case "1":
                        field="hotScore";
                        break;
                    case "2":
                        field="price";
                        break;
                }
                //设置排序条件
                searchSourceBuilder.sort(field,"asc".equals(split[1])? SortOrder.ASC:SortOrder.DESC);
            }else {
                //前端没有传递排序条件设置默认排序
                searchSourceBuilder.sort("hotScore",SortOrder.DESC);
            }
        }

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style=color:red>");
        highlightBuilder.postTags("</span>");
        //关联高亮对象到主查询器对象
        searchSourceBuilder.highlighter(highlightBuilder);

        //设置品牌聚合
        TermsAggregationBuilder brandTermsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        //关联品牌聚合到主查询器对象
        searchSourceBuilder.aggregation(brandTermsAggregationBuilder);

        //设置平台属性聚合
        NestedAggregationBuilder attrTermsAggregationBuilder = AggregationBuilders.nested("attrAgg", "attrs");
        AggregationBuilder attrIdAgg=  AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));
        attrTermsAggregationBuilder.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(attrTermsAggregationBuilder);

        //定制要显示的字段
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest("goods");
        //关联主查询对象到搜索请求对象
        searchRequest.source(searchSourceBuilder);
        System.out.println("dsl:"+searchSourceBuilder.toString());
        return searchRequest;
    }

   /* private SearchResponseVo parseSearchResult(SearchResponse search) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        Map<String, Aggregation> stringAggregationMap = search.getAggregations().getAsMap();

        //品牌 集合

        ParsedLongTerms tmIdAgg = (ParsedLongTerms) stringAggregationMap.get("tmId");
        //遍历 取出id封装到品牌vo  放入封装对象返回
        List<SearchResponseTmVo> collect = tmIdAgg.getBuckets().stream().map(buckey -> {
            SearchResponseTmVo s = new SearchResponseTmVo();
            s.setTmId(Long.parseLong(((Terms.Bucket) buckey).getKeyAsString()));//赋值id
            //id的附属结果获取 名称和图片
            Map<String, Aggregation> tmIdSubMap = ((Terms.Bucket) buckey).getAggregations().asMap();
            //名字
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmIdSubMap.get("tmNameAgg");
            String keyAsString = tmNameAgg.getBuckets().get(0).getKeyAsString();
            s.setTmName(keyAsString);
            //图片

            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmIdSubMap.get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            s.setTmLogoUrl(tmLogoUrl);
            return s;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(collect);//放入

        //list 高亮取出，封装到对象中
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();
        List<Goods> goodsList = new ArrayList<>();
        if (hits1 != null && hits1.length > 0) {
            for (SearchHit searchHit : hits1) {
                String sourceAsString = searchHit.getSourceAsString();
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                //高亮封装title
                Text title = searchHit.getHighlightFields().get("title").fragments()[0];
                goods.setTitle(title.toString());
                goodsList.add(goods);
            }
        }
        //封装到vo对象中
        searchResponseVo.setGoodsList(goodsList);


        //属性 聚合
        //获取平台属性的聚合结果
        ParsedNested attrAgg = (ParsedNested) stringAggregationMap.get("attrAgg");
        //获取平台属性子节点attrIdAgg
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        //获取平台属性子节点attrIdAgg的存储桶
        List<? extends Terms.Bucket> idAggBuckets = attrIdAgg.getBuckets();
        //判断存储桶是否为空
        if (!CollectionUtils.isEmpty(idAggBuckets)) {
            List<SearchResponseAttrVo> searchResponseAttrVos = idAggBuckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                searchResponseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                //获取子节点属性名的聚合结果
                ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
                if (attrNameAgg != null) {
                    List<? extends Terms.Bucket> nameAggBuckets = attrNameAgg.getBuckets();
                    searchResponseAttrVo.setAttrName(nameAggBuckets.get(0).getKeyAsString());
                }
                //获取子节点属性值的聚合结果
                ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                if (attrValueAgg != null) {
                    List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                    List<String> list = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    searchResponseAttrVo.setAttrValueList(list);
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList());
            //设置
            searchResponseVo.setAttrsList(searchResponseAttrVos);
        }
        return searchResponseVo;
    }*/
   //解析响应结果
   private SearchResponseVo parseSearchResult(SearchResponse response){
       SearchHits hits = response.getHits();
       //声明响应对象
       SearchResponseVo searchResponseVo = new SearchResponseVo();
       //获取全部的聚合结果
       Map<String, Aggregation> aggregationMap = response.getAggregations().getAsMap();
       //获取品牌的聚合结果
       ParsedLongTerms tmIdAgg= (ParsedLongTerms) aggregationMap.get("tmIdAgg");
       List<SearchResponseTmVo> searchResponseTmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
           SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
           //设置品牌编号
           searchResponseTmVo.setTmId(Long.parseLong(((Terms.Bucket) bucket).getKeyAsString()));
           //获取子节点的分组结果
           Map<String, Aggregation> tmIdSubMap = ((Terms.Bucket) bucket).getAggregations().asMap();
           //获取子分组的品牌名称
           ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmIdSubMap.get("tmNameAgg");
           //获取品牌名称
           String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
           //           //设置品牌名称到响应对象
           searchResponseTmVo.setTmName(tmName);
           //获取子分组的 品牌logo配图
           ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmIdSubMap.get("tmLogoUrlAgg");
           //获取配图
           String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
           searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
           return searchResponseTmVo;
       }).collect(Collectors.toList());

       //关联设置品牌集合到主响应对象
       searchResponseVo.setTrademarkList(searchResponseTmVoList);

       //获取搜索结果
       SearchHit[] subHits = hits.getHits();
       List<Goods> goodsList=new ArrayList<>();
       if(subHits!=null&&subHits.length>0){
           for (SearchHit subHit : subHits) {
               //获取查询对象，从json转换为对象
               Goods goods = JSON.parseObject(subHit.getSourceAsString(), Goods.class);
               //获取高亮
               if(subHit.getHighlightFields().get("title")!=null){
                   //读取高亮标题
                   Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                   //设置标题到商品对象
                   goods.setTitle(title.toString());
               }
               //添加商品对象到集合
               goodsList.add(goods);
           }
       }

       //设置搜索结果到响应对象
       searchResponseVo.setGoodsList(goodsList);

//获取平台属性的聚合结果
       ParsedNested attrAgg= (ParsedNested) aggregationMap.get("attrAgg");
       //获取平台属性子节点attrIdAgg
       ParsedLongTerms attrIdAgg=   attrAgg.getAggregations().get("attrIdAgg");
       //获取平台属性子节点attrIdAgg的存储桶
       List<? extends Terms.Bucket> idAggBuckets = attrIdAgg.getBuckets();
       //判断存储桶是否为空
       if(!CollectionUtils.isEmpty(idAggBuckets)){
           List<SearchResponseAttrVo> searchResponseAttrVos = idAggBuckets.stream().map(bucket -> {
               SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
               searchResponseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
               //获取子节点属性名的聚合结果
               ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
               if(attrNameAgg!=null) {
                   List<? extends Terms.Bucket> nameAggBuckets = attrNameAgg.getBuckets();
                   searchResponseAttrVo.setAttrName(nameAggBuckets.get(0).getKeyAsString());
               }
               //获取子节点属性值的聚合结果
               ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
               if(attrValueAgg!=null) {
                   List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                   List<String> list = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                   searchResponseAttrVo.setAttrValueList(list);
               }
               return searchResponseAttrVo;
           }).collect(Collectors.toList());

           //设置
           searchResponseVo.setAttrsList(searchResponseAttrVos);

       }

       //设置总记录数
       searchResponseVo.setTotal(hits.getTotalHits());

       return searchResponseVo;
   }
}
