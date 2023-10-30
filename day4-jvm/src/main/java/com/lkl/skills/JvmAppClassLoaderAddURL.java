package com.lkl.skills;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class JvmAppClassLoaderAddURL {

    public static void main(String[] args) {
        // .class文件
        String appPath = "file:/D:/workspace/code";
        URLClassLoader urlClassLoader = (URLClassLoader) JvmAppClassLoaderAddURL.class.getClassLoader();
        try {
            // 反射调用
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            URL url = new URL(appPath);
            addURL.invoke(urlClassLoader, url);
            Class.forName("jvm.Hello"); // 效果跟Class.forName("jvm.Hello").newInstance()一样
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}