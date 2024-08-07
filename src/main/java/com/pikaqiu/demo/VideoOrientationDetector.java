package com.pikaqiu.demo;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

/**
 * <p> VideoOrientationDetector </p>
 *
 * @author xiaoye
 * @version 1.0
 * @date 2024/4/15 9:45
 */
public class VideoOrientationDetector {
    public static String getVideoOrientation(String videoUrl) {
        //获取路径后缀
        String suffix = videoUrl.substring(videoUrl.lastIndexOf(".") + 1);
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoUrl)) {
            grabber.start();
            grabber.setFormat(suffix);
            int height = grabber.getImageHeight();
            int width = grabber.getImageWidth();
            grabber.stop();

            if (width > height) {
                //这是横屏
                return "Landscape";
            } else if (height > width) {
                //这是竖屏z
                return "Portrait";
            } else {
                //这是正方形 默认横屏
                return "Square";
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            return "Error occurred";
        }
    }

    public static void main(String[] args) {
        String videoUrl = "https://qyapitest.qiyucloud.com.cn/douyin-storage/fileStorage/getFileBykey?key=tenant:1/20240411221401714xt793.mp4";
        String orientation = getVideoOrientation(videoUrl);
        System.out.println("The video is in " + orientation + " orientation.");
    }
}
