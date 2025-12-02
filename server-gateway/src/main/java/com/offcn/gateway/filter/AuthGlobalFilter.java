package com.offcn.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.offcn.common.result.Result;
import com.offcn.common.result.ResultCodeEnum;
import com.offcn.common.util.IpUtil;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();//匹配路径的工具
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${authUrls.url}")
    private String authUrls;
    @Override
    /**
     * 过滤请求 url token
     * */
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //拦截请求
        ServerHttpRequest request = exchange.getRequest();
        //url三要素中的url
        String path = request.getURI().getPath();
        //根据url 访问谁，有的路径是不饿能访问的 内部使用的inner
        if (antPathMatcher.match("/**/inner/**",path)){//内部方法不能访问，直接放回响应
            //获取响应
            ServerHttpResponse response = exchange.getResponse();
            //设置响应输入的内容
            return out(response, ResultCodeEnum.LOGIN_AUTH);
        }
        //不是内部方法，检查令牌 token 高内聚
        String uesrId = getUserId(request);
        if ("-1".equals(uesrId)){
            ServerHttpResponse response = exchange.getResponse();
            return out(response,ResultCodeEnum.PERMISSION);
        }
        //用户登录认证，api接口，异步请求，校验用户必须登录
        if (antPathMatcher.match("/api/**/auth/**",path)){
            if (StringUtils.isEmpty(uesrId)){
                ServerHttpResponse response = exchange.getResponse();
                return out(response,ResultCodeEnum.LOGIN_AUTH);
            }
        }
        //验证url 哪些url不需要登录（主页，详情页）哪些登录（购物车，订单，确认订单）
        for (String anthUrl:authUrls.split(",")){
            //如果需要登录的页面，但是此时userId是空，那就不行
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.SEE_OTHER);
            //跳转到登录页面
            response.getHeaders().set(HttpHeaders.LOCATION,"http://www.omall.com/login.html?originUrl="+
                    request.getURI());
            return response.setComplete();
        }
        //检查都通过，放行，放行之前吧用户的状态传递(重新设置)
        if (!(StringUtils.isEmpty(uesrId))){
            request.mutate().header("userId",uesrId).build();
            return chain.filter(exchange);
        }
        return chain.filter(exchange);//过滤完毕，进入路由
    }
    /**
     * 检查请求中的令牌（header，cookie）是否有效，服务器中用redis存储令牌及用户状态（id，ip）
     * 如果检查token合格返回用户id     redis token，josn对象（id，ip）
     * */
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        //去头中获取token
        List<String> tokenList = request.getHeaders().get("token");
        if (tokenList!= null){
            token = tokenList.get(0);
        }else {//cookie中是否有令牌
            MultiValueMap<String, HttpCookie> cookieMultiValueMap = request.getCookies();
            HttpCookie cookie = cookieMultiValueMap.getFirst("token");
            if (cookie!=null){
                token = URLDecoder.decode(cookie.getValue());
            }
        }
        if (!StringUtils.isEmpty(token)){
            //token不为空，需要去和redis中的token进行对比
            String userStr= (String) redisTemplate.opsForValue().get("user:login:"+token);
            JSONObject userJson = JSONObject.parseObject(userStr);//id ,ip
            String ip = userJson.getString("ip");//登录是存储ip
            //此时的ip
            String curIp = IpUtil.getGatwayIpAddress(request);
            //比较两次的ip是否一致
            if (ip.equals(curIp)){
                return userJson.getString("userId");
            }else {
                return "-1";
            }
        }
        return "";
    }

    //设置响应对象response （头，格式，编码）
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);//UTF-8
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
        return response.writeWith(Mono.just(wrap));
    }
}
