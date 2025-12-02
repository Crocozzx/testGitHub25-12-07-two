package com.offcn.gateway.filter;

import java.util.Scanner;

public class 黑掉微信程序 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入手机号:");
        String next2 = sc.next();
        System.out.println("请输入微信号：");
        String next = sc.next();
        System.out.println("请输入姓名:");
        String next1 = sc.next();
        if (next.equals("") && next1.equals("")){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("失败");
        }else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("成功");
        }
    }
}
