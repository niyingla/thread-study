package com.pikaqiu.demo.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @Author: zyong
 * @Date: 2018/10/31 14:38
 * @Version 1.0
 * 类扫描工具类
 */
@Slf4j
public class ClassScanUtils {


    public static void main(String[] args) {


        System.out.println(getClasses( "com.pikaqiu" ));
    }


    /**
     * 从包package中获取所有的Class
     * @param pack
     * @return
     */
    public static Set<Class<?>> getClasses(String pack){

        if(pack == null){
            log.error(" load pack is not null !!");
            return null;
        }
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = pack.replace('.', '/');
        try{
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()){
                URL url = dirs.nextElement();
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(pack, filePath, recursive, classes);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    public static void findAndAddClassesInPackageByFile(
            String packageName,
            String packagePath,
            final boolean recursive,
            Set<Class<?>> classes){
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter(){
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file){
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        for (File file : dirfiles){
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            }else{
                String className = file.getName().substring(0, file.getName().length() - 6);
                try{
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                }catch (ClassNotFoundException e){
                    log.info(e.getMessage());
                }
            }
        }
    }


}
