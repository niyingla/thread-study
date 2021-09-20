package com.pikaqiu.java8;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 10479
 */
public class FileUtils {
    /**
     * 储存扫描文件目录下的文件列表
     */
    private static ArrayList<File> scanFiles = new ArrayList<File>();


    public static void main(String[] args) throws Exception{
        delSuffix();
    }

    private static void delSuffix() throws Exception {
        //扫描文件目录
        ArrayList<File> objects = scanFilesWithRecursion("E:\\学习视频\\面向未来微服务Spring Cloud Alibaba微服务从入门到进阶（全）");

        for (int i = 0; i < objects.size(); i++) {
            File file = objects.get(i);
            String name = file.getName();
            if (name.contains("【51优质资源 www.51data.org】")) {
                file.renameTo(new File(file.getAbsolutePath().replace("【51优质资源 www.51data.org】", "")));
            }

        }
    }


    /**
     * 删除相同文件
     * @throws Exception
     */
    private static void delSameFile() throws Exception {
        //扫描文件目录
        ArrayList<File> objects = scanFilesWithRecursion("E:\\学习视频");

        //转化成文件名字map
        Map<String, File> fileMap = objects.stream()
                .collect(Collectors.toMap(File::getAbsolutePath, item -> item));

        //寻找是否存在相同文件
        ArrayList<String> fileNames = new ArrayList<>();
        for (Map.Entry<String, File> queueFile : fileMap.entrySet()) {
            String sameFileName = queueFile.getValue().getAbsolutePath().replace(".mp4", "") + "(1).mp4";
            if (fileMap.containsKey(sameFileName) && fileMap.get(sameFileName).length() == queueFile.getValue().length()) {
                fileNames.add(sameFileName);
            }
        }
        //循环删除文件
        for (String fileName : fileNames) {
            File file = fileMap.get(fileName);
            file.delete();
        }
    }

    /**
     * @return ArrayList<Object>
     * @author
     * @time 2017年11月3日
     */
    public static ArrayList<File> scanFilesWithRecursion(String folderPath) throws Exception{
        ArrayList<String> dirctorys = new ArrayList<String>();
        File directory = new File(folderPath);
        if(!directory.isDirectory()){
            throw new Exception('"' + folderPath + '"' + " input path is not a Directory , please input the right path of the Directory. ^_^...^_^");
        }
        if(directory.isDirectory()){
            File [] filelist = directory.listFiles();
            for(int i = 0; i < filelist.length; i ++){
                /**如果当前是文件夹，进入递归扫描文件夹**/
                if(filelist[i].isDirectory()){
                    //绝对路径名字符串
                    dirctorys.add(filelist[i].getAbsolutePath());
                    /**递归扫描下面的文件夹**/
                    scanFilesWithRecursion(filelist[i].getAbsolutePath());
                }
                /**非文件夹**/
                else{
                    scanFiles.add(filelist[i]);
                }
            }
        }
        return scanFiles;
    }


}
