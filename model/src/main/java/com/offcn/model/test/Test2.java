package com.offcn.model.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Test2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(new Supplier<Integer>() {
            @Override
            public Integer get() {
                System.out.println(Thread.currentThread().getName() + "\t completableFuture");
                int i = 10/0;
                return 1024;
            }
        }).thenApply(new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer o) {
                System.out.println("thenApply方法，上次返回结果：" + o);
                return o * 2;
            }
        }).whenComplete(new BiConsumer<Integer, Throwable>() {
            @Override
            public void accept(Integer o, Throwable throwable) {
                System.out.println("-------o=" + o);
                System.out.println("-------throwable=" + throwable);
            }
        }).exceptionally(new Function<Throwable, Integer>() {
            @Override
            public Integer apply(Throwable throwable) {
                System.out.println("throwable=" + throwable);
                return 6666;
            }
        });
        System.out.println(future.get());
        /**有异常时
         *ForkJoinPool.commonPool-worker-1	 completableFuture
         * -------o=null
         * -------throwable=java.util.concurrent.CompletionException: java.lang.ArithmeticException: / by zero
         * throwable=java.util.concurrent.CompletionException: java.lang.ArithmeticException: / by zero
         * 6666
         *
         *
         * */
    }
}
