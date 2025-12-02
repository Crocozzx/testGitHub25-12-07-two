package com.offcn.model.test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class Test4 {
    public static void main(String[] args) {
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> {
            delaySec(6);
            System.out.println("线程1执行");
            return "one";
        });
        CompletableFuture<String> futureB = CompletableFuture.supplyAsync(() -> {
            delaySec(4);
            System.out.println("线程2执行");
            return "two";
        });
        //当2个线程任意一个执行完毕
        CompletableFuture.anyOf(futureA,futureB).whenComplete(new BiConsumer<Object, Throwable>() {
            @Override
            public void accept(Object o, Throwable throwable) {
                System.out.println("执行完毕:"+o);
            }
        });
        /**打印结果
         * 线程2执行
         * 执行完毕:two
         * 线程1执行
         *
         * */
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
