package com.offcn.model.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Test1 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //main线程

        //开启新线程
        CompletableFuture<Object> exceptionally = CompletableFuture.supplyAsync(new Supplier<Object>() {
            @Override
            public Object get() {
                //业务
                System.out.println(Thread.currentThread().getName() + "\t" + "Test1");
                //异常
                //int i = 10 / 0;
                return 1024;//返回值
            }
        }).whenComplete(new BiConsumer<Object, Throwable>() {//表示会调函数，当上面的方法完全执行结束，执行该方法
            @Override
            public void accept(Object o, Throwable throwable) {
                System.out.println("-------o=" + o.toString());
                System.out.println("-------throwable=" + throwable);
            }
        }).exceptionally(new Function<Throwable, Object>() {//表示上面的方法发生异常时执行的方法
            @Override
            public Object apply(Throwable throwable) {
                System.out.println("throwable=" + throwable);
                return 6666;
            }
        });
        System.out.println(exceptionally.get());//最后方法的返回值
        /**没有异常时
         * ForkJoinPool.commonPool-worker-1	Test1
         * -------o=1024
         * -------throwable=null
         * 1024
         *
         *
         * */
    }
}
