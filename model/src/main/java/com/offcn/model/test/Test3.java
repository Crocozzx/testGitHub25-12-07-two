package com.offcn.model.test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Test3 {
    public static void main(String[] args) {
        //创建线程池    线程池无论启动多少个集群线程池只有一个
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(50, 500,
                30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));
        //线程1执行返回的结果：hello
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> "hello");
        //等同于下面
        /*CompletableFuture.supplyAsync(new Supplier<Object>() {
            @Override
            public Object get() {
                return "hello";
            }
        });*/
        //线程3获取线程2 的执行的结果，也就是说这个线程和下面的线程必须时上面线程2的方法结束之后返回值，
        // 才可以开启下面2个线程执行
        CompletableFuture<Void> voidCompletableFuture = futureA.thenAcceptAsync((s) -> {
            delaySec(4);
            printCurrTime(s + "第3个线程");
        }, threadPoolExecutor);//表示使用线程池中的线程
    //线程4获取到线程2的执行结果
        futureA.thenAcceptAsync((s)->{
            delaySec(3);
            printCurrTime(s+"第4个线程");
        },threadPoolExecutor);
        try {
            System.in.read();//阻塞
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**打印结果 ：因为线程3和4是并发的
         * hello第4个线程
         * hello第3个线程
         *
         *
         * */
    }
    private static void delaySec(int i) {
        try {
            Thread.sleep(i*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printCurrTime(String str) {
        System.out.println(str);
    }
}
