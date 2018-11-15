package com.example.text.demo;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import java.io.*;

/**
 * @program: text
 * @description:
 * @author: xiaoye
 * @create: 2018-11-15 10:16
 **/

@Slf4j
public class PicUtils {

//    public static void main(String[] args) throws IOException {
//        byte[] bytes = FileUtils.readFileToByteArray(new File("D:\\1.jpg"));
//        long l = System.currentTimeMillis();
//        bytes = PicUtils.compressPicForScale(bytes, 300, "x");// 图片小于300kb
//        System.out.println(System.currentTimeMillis() - l);
//        FileUtils.writeByteArrayToFile(new File("D:\\dd1.jpg"), bytes);
//    }

    /**
     * 根据指定大小压缩图片
     *
     * @param imageBytes  源图片字节数组
     * @param desFileSize 指定图片大小，单位kb
     * @param imageId     影像编号
     * @return 压缩质量后的图片字节数组
     */
    public static byte[] compressPicForScale(byte[] imageBytes, long desFileSize, String imageId) {
        if (imageBytes == null || imageBytes.length <= 0 || imageBytes.length < desFileSize * 1024) {
            return imageBytes;
        }
        long srcSize = imageBytes.length;
        double accuracy = getAccuracy(srcSize / 1024);
        try {
            while (imageBytes.length > desFileSize * 1024) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageBytes.length);
                Thumbnails.of(inputStream)
                        .scale(accuracy)
                        .outputQuality(accuracy)
                        .toOutputStream(outputStream);
                imageBytes = outputStream.toByteArray();
            }
            log.info("【图片压缩】imageId={} | 图片原大小={}kb | 压缩后大小={}kb",
                    imageId, srcSize / 1024, imageBytes.length / 1024);
        } catch (Exception e) {
            log.error("【图片压缩】msg=图片压缩失败!", e);
        }
        //返回结果就是这一行
        return imageBytes;
    }

    /**
     * 自动调节精度(经验数值)
     *
     * @param size 源图片大小
     * @return 图片压缩质量比
     */
    private static double getAccuracy(long size) {

        double accuracy;
        if (size < 900) {
            accuracy = 0.85;
        } else if (size < 2047) {
            accuracy = 0.6;
        } else if (size < 3275) {
            accuracy = 0.44;
        } else {
            accuracy = 0.4;
        }
        return accuracy;
    }

    public static void main(String[] args)throws Exception {


//        new FileReader("C:\\Users\\10479\\Downloads\\timg (2).jpg").read(bytes)

        File file = new File("C:\\Users\\10479\\Downloads\\timg (2).jpg");

        FileInputStream fileInputStream = new FileInputStream(file);

        byte[] bytes = new byte[1024 * 1024];

        int read = fileInputStream.read(bytes,0,Integer.valueOf(file.length()+""));

        byte[] bytesTarget = compressPicForScale(bytes, 128, "1");

        File fileTarget = new File("C:\\Users\\10479\\Downloads\\timg (3).jpg");

        FileOutputStream fileOutputStream = new FileOutputStream(fileTarget);

        fileOutputStream.write(bytesTarget);

        fileOutputStream.flush();

        fileInputStream.close();

        fileInputStream.close();

    }

}
