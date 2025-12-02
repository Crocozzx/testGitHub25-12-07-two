package com.offcn.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


//@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//该父类因为引用了数据源，我们当前项目不需要连接数据库
////所以需要关闭该数据源 ，不排除这个jar 报错
//@ComponentScan({"com.offcn"})//熔断的时候 该本地实现上面添加的就是@Component注解，找到该注解
//@EnableDiscoveryClient//让别人找到
//@EnableFeignClients(basePackages = {"com.offcn"})
//public class ServiceItemApplication {
//    public static void main(String[] args) {
//        SpringApplication.run(ServiceItemApplication.class,args);
//    }
//}
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据源自动配置
@ComponentScan({"com.offcn"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.offcn"})
public class ServiceItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceItemApplication.class, args);
    }

}
