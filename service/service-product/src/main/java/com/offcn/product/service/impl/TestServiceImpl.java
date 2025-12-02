package com.offcn.product.service.impl;

import com.alibaba.nacos.client.utils.StringUtils;
import com.offcn.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {
    @Autowired //redis客户端
    private StringRedisTemplate redisTemplate;


    /*public void testLock() {
        String s = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", s, 3, TimeUnit.SECONDS);
        if (lock){
            //业务逻辑
            // 查询redis中的num值
            String value = (String)this.redisTemplate.opsForValue().get("num");
            // 没有该值return
            if (StringUtils.isEmpty(value)){
                return ;
            }
            // 有值就转成成int
            int num = Integer.parseInt(value);
            // 把redis中的num值+1
            ++num;
            this.redisTemplate.opsForValue().set("num", String.valueOf(num));
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            //这个lua脚本和上面出现问题的那个判断如果不是当前的uuid你不能删除
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);//把lua脚本给他
            redisScript.setResultType(Long.class);//表示如果不是该s就返回0
            redisTemplate.execute(redisScript, Arrays.asList("lock"),s);//使用redis客户端完成删除锁的操作，这样不会有判断完
            //删除别的线程锁的问题
            //Arrays.asList("lock")需要的是数组的参数，
        }else {
            //重试
            testLock();
        }
    }*/
    @Autowired
    private RedissonClient redissonClient;//引入客户端
    public void testLock() {
        RLock lock = redissonClient.getLock("lock");//获得 lock该锁
        lock.lock();//加该锁
        //业务逻辑
        // 查询redis中的num值
        String value = (String)this.redisTemplate.opsForValue().get("num");
        // 没有该值return
        if (StringUtils.isEmpty(value)){
            return ;
        }
        // 有值就转成成int
        int num = Integer.parseInt(value);
        // 把redis中的num值+1
        ++num;
        this.redisTemplate.opsForValue().set("num", String.valueOf(num));
        lock.unlock();//解锁  


    }
}
