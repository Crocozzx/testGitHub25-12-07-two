package com.offcn.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offcn.common.constant.RedisConst;
import com.offcn.model.product.*;
import com.offcn.product.mapper.*;
import com.offcn.product.service.ManageService;
import com.sun.org.apache.regexp.internal.RE;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings({"all"})
@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrinfoMapper baseAttrinfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;



    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;


    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;




    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        // select * from baseCategory2 where Category1Id = ?
        QueryWrapper queryWrapper = new QueryWrapper<BaseCategory2>();
        queryWrapper.eq("category1_id",category1Id);
        List<BaseCategory2> baseCategory2List = baseCategory2Mapper.selectList(queryWrapper);
        return baseCategory2List;
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        // select * from baseCategory3 where Category2Id = ?
        QueryWrapper queryWrapper = new QueryWrapper<BaseCategory3>();
        queryWrapper.eq("category2_id",category2Id);
        return baseCategory3Mapper.selectList(queryWrapper);
    }


    @Override//查询结果，基本属性，并且包含所属的属性值
    public List<BaseAttrInfo> getBaseAttrInfo(Long category1Id, Long category2Id, Long category3Id) {
        //可以使用java代码的格式进行条件查询，也可以使用xml的格式进行条件查询
        /*QueryWrapper<BaseAttrInfo> qw = new QueryWrapper<>();
        if (category1Id != null && category1Id.longValue() > 0){
            qw.or();
            qw.eq("category_id",category1Id);
            qw.eq("category_level",1);
        }
        if (category2Id != null && category2Id.longValue() > 0){
            qw.or();
            qw.eq("category_id",category2Id);
            qw.eq("category_level",2);
        }
        if (category3Id != null && category3Id.longValue() > 0){
            qw.or();
            qw.eq("category_id",category3Id);
            qw.eq("category_level",3);
        }
        List<BaseAttrInfo> baseAttrInfos = baseAttrinfoMapper.selectList(qw);
        for (BaseAttrInfo b : baseAttrInfos){
            QueryWrapper<BaseAttrValue> qw1 = new QueryWrapper<>();
            qw1.eq("attr_id",b.getId());
            List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(qw1);
            b.setAttrValueList(baseAttrValues);
        }*/
        List<BaseAttrInfo> attrValueList = baseAttrinfoMapper.getAttrValueList(category1Id, category2Id, category3Id);
        return attrValueList;
    }
    @Transactional
    @Override
    public void save(BaseAttrInfo baseAttrInfo) {
        // 方法名 save是保存的意思，所以修改和添加是在一个方法中
        //如何判断修改和添加，看参数中是否由id，如果由id那么就是修改，如果没有id那么就是添加
        //id==null?add:update
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId() != 0 ){
            //修改
            baseAttrinfoMapper.updateById(baseAttrInfo);
        }else {
            //添加
            int insert = baseAttrinfoMapper.insert(baseAttrInfo);
        }


        QueryWrapper<BaseAttrValue> qw = new QueryWrapper<>();
        qw.eq("attr_id",baseAttrInfo.getId());
        baseAttrValueMapper.delete(qw);

        for (BaseAttrValue b : baseAttrInfo.getAttrValueList()){
            b.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(b);
        }




    }

    @Override
    public BaseAttrInfo getAttrInfo(Long id) {

        BaseAttrInfo baseAttrInfo = baseAttrinfoMapper.selectById(id);

        QueryWrapper<BaseAttrValue> qw = new QueryWrapper<>();
        qw.eq("attr_id",baseAttrInfo.getId());
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(qw);
        baseAttrInfo.setAttrValueList(baseAttrValues);

        return baseAttrInfo;
    }

    @Override//更具字段进行分页
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> qw = new QueryWrapper<>();
        qw.eq("category3_id",spuInfo.getCategory3Id());
        qw.orderByDesc("id");
        IPage<SpuInfo> spuInfoIPage = spuInfoMapper.selectPage(pageParam, qw);
        return spuInfoIPage;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrMapper.selectList(null);
        return baseSaleAttrs;
    }
    @Transactional
    @Override
    public void save(SpuInfo spuInfo) {
        //先添加1
        int insert = this.spuInfoMapper.insert(spuInfo);
        //添加图片的多
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage:spuImageList){
            spuImage.setSpuId(spuInfo.getId());
            int insert1 = this.spuImageMapper.insert(spuImage);
        }
        //添加属性的多
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr:spuSaleAttrList){
            spuSaleAttr.setSpuId(spuInfo.getId());
            int insert1 = this.spuSaleAttrMapper.insert(spuSaleAttr);
            //添加属性值的多
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue:spuSaleAttrValueList){
                //因为属性值里面的字段前端不提交（是商品表的id），所以需要手动设置
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                this.spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            }

        }
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> qw = new QueryWrapper<>();
        qw.eq("spu_id",spuId);
        List<SpuImage> spuImages = this.spuImageMapper.selectList(qw);
        return spuImages;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
    /*
        skuInfo 库存单元表 --- spuInfo！
        skuImage 库存单元图片表 --- spuImage!
        skuSaleAttrValue sku销售属性值表{sku与销售属性值的中间表} --- skuInfo ，spuSaleAttrValue
        skuAttrValue sku与平台属性值的中间表 --- skuInfo ，baseAttrValue
     */
        skuInfoMapper.insert(skuInfo);
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0) {

            // 循环遍历
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        // 调用判断集合方法
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }
    }

    @Override
    public IPage<SkuInfo> getPage(Page<SkuInfo> page) {
        QueryWrapper<SkuInfo> qw = new QueryWrapper<>();
        qw.orderByAsc("id");
        IPage<SkuInfo> skuInfoIPage = skuInfoMapper.selectPage(page, qw);
        return skuInfoIPage;
    }

    @Override
    public void onSale(Long skuId) {
        //修改为1
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
        //然后添加索引

    }

    @Override
    public void cancelSale(Long skuId) {

        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
        //删除索引
    }

    @Override
    public SkuInfo getSkuInfo(Long skuId) {

        // 1. 从redis查
        // 2. 判断结果==null？ 查询mysql 并放入到redis中：return

        // // 使用框架redisson解决分布式锁！
        return getSkuInfoRedisson(skuId);

        // return getSkuInfoRedis(skuId);
    }
    public SkuInfo getSkuInfoDB(Long skuId) {

        // 查一
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

        // 查多 -- 图片
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(queryWrapper);

        // 把 多 封装到 一
        skuInfo.setSkuImageList(skuImageList);

        return skuInfo;

    }
    /**
     * 使用redisson 做分布式锁 自动
     *
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoRedisson(Long skuId) {

        SkuInfo skuInfo = null;
        /*沙雕  猜一猜我是谁？？？？？？？？？？*/
        try {
            // 缓存存储数据：key-value
            // 定义key sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            // 获取里面的数据 redis 有五种数据类型 那么我们存储的商品详情 使用的是那种数据类型？
            //获取缓存数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            // 如果缓存数据中获取的数据为空
            if (skuInfo == null) {
                // 直接获取数据库中的数据， 可能会造成缓存击穿。所以在这个位置需要加锁
                // 第二种：redisson
                // 定义锁的key sku:skuId:lock  set k1 v1 px 10000 nx
                String locKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(locKey);
            /*
            第一种： lock.lock();
            第二种:  lock.lock(10,TimeUnit.SECONDS);
            第三种： lock.tryLock(100,10,TimeUnit.SECONDS);
             */
                // 尝试加锁
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                // 上锁
                // Boolean isExist = redisTemplate.opsForValue().setIfAbsent(locKey, uuid, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (res) {
                    try {
                        // 处理业务逻辑 获取数据库中的数据
                        // 真正获取数据库中的数据 {数据库中到底有没有这个数据 = 防止缓存穿透}
                        skuInfo = getSkuInfoDB(skuId);
                        // 从数据库中获取到的数据就是空的
                        if (skuInfo == null) {
                            // 为了避免缓存穿透 应该给空的对象放入缓存
                            SkuInfo skuInfo1 = new SkuInfo();// 对象的地址
                            redisTemplate.opsForValue().set(skuKey, skuInfo1, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return skuInfo1;
                        }
                        //查询数据库的时候有值
                        redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);


                        return skuInfo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        // 使用redis 用的是lua 脚本删除 ，但是现在用么？ lock.unlock
                        // 解锁
                        lock.unlock();
                    }
                } else {
                    // 其他线程等待
                    Thread.sleep(1000);

                    return getSkuInfoRedisson(skuId);
                }

            } else {
                return skuInfo;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
// 为了防止缓存宕机：从数据库中获取数据
        return getSkuInfoDB(skuId);
    }



    /*@Override
    public SkuInfo getSkuInfo(Long skuId){
        //1从redis查
        //2判断结果==null?查询mysql 并放入redis中: return;
        return this.getSkuInfoRedis(skuId);
    }*/
        //手动通过restTemlate模板
    /*public SkuInfo getSkuInfoRedis(Long skuId) {
        SkuInfo skuInfo = null;
        //定义key sku:skuid:info
        String skukey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
        skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skukey);
        if (skuInfo == null){//表示从mysql中查询
            //分布式锁
            String s1 = UUID.randomUUID().toString();
            Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(skukey, s1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
            if (aBoolean){//上锁成功
                skuInfo = this.getSkuInfoDB(skuId);
                //判断skuInfo对象是否在mysql中查询到，如果查询到放入redis中，如果查不到该对象，手动new一个对象放入
                if (skuInfo == null){//解决redis面临3个问题中的穿透
                    SkuInfo skuInfo1 = new SkuInfo();
                    redisTemplate.opsForValue().set(skukey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                    return skuInfo1;
                }
                //如果查到该对象放入redis中，并且返回
                redisTemplate.opsForValue().set(skukey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                //业务完毕，进行解锁
                // 解锁：使用lua 脚本解锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 设置lua脚本返回的数据类型
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                // 设置lua脚本返回类型为Long
                redisScript.setResultType(Long.class);
                redisScript.setScriptText(script);
                // 删除key 所对应的 value
                redisTemplate.execute(redisScript, Arrays.asList(skukey),s1);
                //返回结果
                return skuInfo;
            }else {//没有上锁成功，重复抢锁
                return getSkuInfo(skuId);
            }

        }else {//reids中不=null，查询直接返回
            return skuInfo;
        }
    }*/
    /*@Autowired
    private RedissonClient redissonClient;
    //自动通过redissonCliet
    public SkuInfo getSkuInfoRedis(Long skuId) {
        SkuInfo skuInfo = null;
        //定义key sku:skuid:info
        String skukey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
        //redis是单线程，就算没有锁控制也会自动排队
        skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skukey);
        if (skuInfo == null){//表示从mysql中查询   mysql不是单线程通过缓存锁的机制来控制
            String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
            //分布式锁
            RLock lock = redissonClient.getLock(lockKey);
            boolean res = false;
            try {
                res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if (res){//上锁成功
                skuInfo = this.getSkuInfoDB(skuId);
                //判断skuInfo对象是否在mysql中查询到，如果查询到放入redis中，如果查不到该对象，手动new一个对象放入
                if (skuInfo == null){//解决redis面临3个问题中的穿透
                    SkuInfo skuInfo1 = new SkuInfo();
                    redisTemplate.opsForValue().set(skukey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                    return skuInfo1;
                }
                //如果查到该对象放入redis中，并且返回
                redisTemplate.opsForValue().set(skukey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                //返回结果
                return skuInfo;
            }else {//没有上锁成功，重复抢锁
                return getSkuInfo(skuId);
            }
        }else {//reids中不=null，查询直接返回
            return skuInfo;
        }

    }*/
        //自己写的
    /*private SkuInfo getSkuInfoRedisson(Long skuId) {
        String skukey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skukey);
        if (skuInfo == null){
            String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;//锁的key
            RLock lock = redissonClient.getLock(lockKey);
            boolean res = false;
            try {
                res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (res){
                    skuInfo = getSkuInfoDB(skuId);
                    if (skuInfo == null){
                        SkuInfo s1 = new SkuInfo();
                        redisTemplate.opsForValue().set(skukey,s1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                        return s1;
                    }
                    redisTemplate.opsForValue().set(skukey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    return skuInfo;
                }else {
                    return getSkuInfoRedisson(skuId);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                lock.unlock();//手动去放锁
            }
        }
        return skuInfo;
    }*/
    /*------------------
    private SkuInfo getSkuInfoRedisson(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            // 缓存存储数据：key-value
            // 定义key sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            // 获取里面的数据？ redis 有五种数据类型 那么我们存储商品详情 使用哪种数据类型？
            // 获取缓存数据
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            // 如果从缓存中获取的数据是空
            if (skuInfo==null){
                // 直接获取数据库中的数据，可能会造成缓存击穿。所以在这个位置，应该添加锁。
                // 第二种：redisson
                // 定义锁的key sku:skuId:lock  set k1 v1 px 10000 nx
                String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);
            *//*
            第一种： lock.lock();
            第二种:  lock.lock(10,TimeUnit.SECONDS);
            第三种： lock.tryLock(100,10,TimeUnit.SECONDS);
             *//*
                // 尝试加锁
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (res){
                    try {
                        // 处理业务逻辑 获取数据库中的数据
                        // 真正获取数据库中的数据 {数据库中到底有没有这个数据 = 防止缓存穿透}
                        skuInfo = getSkuInfoDB(skuId);
                        // 从数据库中获取的数据就是空
                        if (skuInfo==null){
                            // 为了避免缓存穿透 应该给空的对象放入缓存
                            SkuInfo skuInfo1 = new SkuInfo(); //对象的地址
                            redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            return skuInfo1;
                        }
                        // 查询数据库的时候，有值
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);

                        // 使用redis 用的是lua 脚本删除 ，但是现在用么？ lock.unlock
                        return skuInfo;

                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        // 解锁：
                        lock.unlock();
                    }
                }else {
                    // 其他线程等待
                    Thread.sleep(1000);
                    return getSkuInfoRedisson(skuId);
                }
            }else {

                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 为了防止缓存宕机：从数据库中获取数据
        return getSkuInfoDB(skuId);
    }*/
        //从数据库中查
    /*public SkuInfo getSkuInfoDB(Long skuId){
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        // 根据skuId 查询图片列表集合
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(queryWrapper);

        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }*/

    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if(null != skuInfo) {
            return skuInfo.getPrice();
        }
        return new BigDecimal("0");
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        Map<Object, Object> map = new HashMap<>();
        // key = 125|123 ,value = 37
        List<Map> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        if (mapList != null && mapList.size() > 0) {
            // 循环遍历
            for (Map skuMap : mapList) {
                // key = 125|123 ,value = 37
                map.put(skuMap.get("value_ids"), skuMap.get("sku_id"));
            }
        }
        return map;
    }

    @Override
    public List<JSONObject> getBaseCategoryList() {
        List<JSONObject>  list = new ArrayList<>();
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);
        Map<Long, List<BaseCategoryView>> collect = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        for (Map.Entry<Long, List<BaseCategoryView>> entry1: collect.entrySet()){
            Long key = entry1.getKey();
            List<BaseCategoryView> value = entry1.getValue();
            int index = 1;
            JSONObject j1 = new JSONObject();
            j1.put("index",index);
            j1.put("categoryId",key);
            j1.put("categoryName",value.get(0).getCategory1Name());
            List<JSONObject> list2 = new ArrayList<>();
            j1.put("categoryChild",list2);
            index++;
            Map<Long, List<BaseCategoryView>> collect1 = value.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> entry2:collect1.entrySet()){
                List<BaseCategoryView> value1 = entry2.getValue();
                Long key1 = entry2.getKey();
                JSONObject j2 = new JSONObject();
                j2.put("index",index);
                j2.put("categoryId",key1);
                j2.put("categoryName",value1.get(0).getCategory2Name());
                list2.add(j2);
                List<JSONObject>  list3 = new ArrayList<>();
                value1.stream().forEach(c->{
                    JSONObject j3 = new JSONObject();
                    j3.put("categoryId",c.getCategory3Id());
                    j3.put("categoryName",c.getCategory3Name());
                    list3.add(j3);
                });
                j2.put("categoryChild",list3);
            }
            list.add(j1);
        }
        return list;
    }

    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        BaseTrademark baseTrademark = this.baseTrademarkMapper.selectById(tmId);
        return baseTrademark;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        return this.baseAttrinfoMapper.getAttrList(skuId);
    }
   /*@Override
   public List<JSONObject> getBaseCategoryList() {
       //声明一个json对象集合
       List<JSONObject> list=new ArrayList<>();
       //获取所有的分类数据集合
       List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
       //循环分类集合，按照一级分类编号进行分组
       Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

       //定义分类级别
       int index=1;

       //获取一级分类下，全部数据，遍历
       for (Map.Entry<Long, List<BaseCategoryView>> entry : category1Map.entrySet()) {
           //获取一级分类编号
           Long category1Id = entry.getKey();
           //获取一级分类对应的二级分类集合
           List<BaseCategoryView> category2List = entry.getValue();
           //创建json对象
           JSONObject category1 = new JSONObject();
           //category1.put("index",index);
           category1.put("categoryId",category1Id);
           //一级分类的名称
           category1.put("categoryName",category2List.get(0).getCategory1Name());
           //累加分类级别
           index++;
           //循环二级分类集合，按照二级分类编号进行分组
           Map<Long, List<BaseCategoryView>> category2Map = category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

           // 声明二级分类对象集合
           List<JSONObject> category2Child = new ArrayList<>();
           //遍历二级分类分组集合
           for (Map.Entry<Long, List<BaseCategoryView>> entry2 : category2Map.entrySet()) {
               //获取二级分类编号
               Long category2Id = entry2.getKey();
               //获取该二级分类所包含的三级分类数据集合
               List<BaseCategoryView> category3List = entry2.getValue();
               //创建二级分类json对象
               JSONObject category2 = new JSONObject();
               category2.put("categoryId",category2Id);
               category2.put("categoryName",category3List.get(0).getCategory2Name());
               //把二级分类对象加入到二级分类集合
               category2Child.add(category2);

               //创建三级分类对象集合
               List<JSONObject> category3Child=new ArrayList<>();
               //循环三级分类集合，按照三级分类编号进行分组
               category3List.stream().forEach(category3View ->{
                   JSONObject category3 = new JSONObject();
                   category3.put("categoryId",category3View.getCategory3Id());
                   category3.put("categoryName",category3View.getCategory3Name());
                   //把三级分类对象加入到三级分类集合
                   category3Child.add(category3);
               });
               //把三级分类集合放入二级分类对象
               category2.put("categoryChild",category3Child);

           }
           //把二级分类集合放入一级分类对象
           category1.put("categoryChild",category2Child);
           //把一级放到总集合
           list.add(category1);
       }
       return list;
   }*/


}
