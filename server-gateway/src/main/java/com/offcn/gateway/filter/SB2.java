package com.offcn.gateway.filter;

import java.util.Scanner;

public class SB2 {
    public static void main(String[] args) {
        /*for (int i = 0;i<=100 ;i++){
            System.out.println("王一爽傻逼");
        }*/
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入微信号");
        String next1 = sc.next();
        System.out.println("请输入姓名");
        String next2 = sc.next();
        if (next1.equals("") && next2.equals("")){
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
