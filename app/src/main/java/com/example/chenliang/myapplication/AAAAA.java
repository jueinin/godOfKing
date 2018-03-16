package com.example.chenliang.myapplication;

import android.os.Environment;

/**
 * Created by chenliang on 2018/1/12.
 */

public class AAAAA {
    public static void main(String[] args) {
        String a="1 056";
        String b=a.replaceAll("\\s+","");

        System.out.println(Environment.getExternalStorageDirectory());
        System.out.println(b);
    }
}
