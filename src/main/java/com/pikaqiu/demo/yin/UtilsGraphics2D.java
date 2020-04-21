package com.pikaqiu.demo.yin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @program: thread-study
 * @description: 生成印章
 * @author: xiaoye
 * @create: 2019-06-03 14:28
 **/
public class UtilsGraphics2D {


    private static final int WIDTH = 500;//图片宽度
    private static final int HEIGHT = 500;//图片高度

    public static void main(String[] args) throws Exception {

/*        BufferedImage image = startGraphics2D("够字数深圳市中农易讯信息技术有限公司", "财务章");
        try {
            String filePath = "E:\\" + System.currentTimeMillis() + ".png";
            ImageIO.write(image, "png", new File(filePath)); //将其保存在E:\\下，得有这个目录
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/

        BufferedImage image = getSquarePng("财务印");
        try {
            String filePath = "E:\\" + System.currentTimeMillis() + ".png";
            ImageIO.write(image, "png", new File(filePath)); //将其保存在E:\\下，得有这个目录
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static BufferedImage startGraphics2D(String message, String centerName) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        String dateStr = formatter.format(new Date());
        return startGraphics2D(message, centerName, "");

    }

    public static BufferedImage startGraphics2D(String message, String centerName, Date signDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        String dateStr = formatter.format(signDate);
        return startGraphics2D(message, centerName, dateStr);
    }

    public static BufferedImage startGraphics2D(String message, String centerName, String year) {
        // 定义图像buffer
        BufferedImage buffImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();
        // 增加下面代码使得背景透明
        buffImg = g.getDeviceConfiguration().createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        g.dispose();
        g = buffImg.createGraphics();
        // 背景透明代码结束

        g.setColor(Color.RED);
        //设置锯齿圆滑
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //绘制圆
        int radius = HEIGHT / 3;//周半径
        int CENTERX = WIDTH / 2;//画图所出位置
        int CENTERY = HEIGHT / 2;//画图所处位置

        Ellipse2D circle = new Ellipse2D.Double();
        circle.setFrameFromCenter(CENTERX, CENTERY, CENTERX + radius, CENTERY + radius);
        g.setStroke(new BasicStroke(10));//设置圆的宽度
        g.draw(circle);

        //绘制中间的五角星
        g.setFont(new Font("宋体", Font.BOLD, 120));
        g.drawString("★", CENTERX - (120 / 2), CENTERY + (120 / 3));

        //添加章名
        g.setFont(new Font("宋体", Font.LAYOUT_LEFT_TO_RIGHT, 25));// 写入签名
        g.drawString(centerName, CENTERX - (centerName.length() * 12), CENTERY + (30 + 50));

        //添加年份
        g.setFont(new Font("宋体", Font.LAYOUT_LEFT_TO_RIGHT, 20));// 写入签名
        g.drawString(year, CENTERX - (65), CENTERY + (30 + 80));

        //根据输入字符串得到字符数组
        String[] messages2 = message.split("", 0);
        String[] messages = new String[messages2.length - 1];
        System.arraycopy(messages2, 0, messages, 0, messages2.length - 1);

        //输入的字数
        int ilength = messages.length;

        //设置字体属性
        int fontsize = 40;  //18个字40 20个字36
        Font f = new Font("Serif", Font.BOLD, fontsize);

        FontRenderContext context = g.getFontRenderContext();
        Rectangle2D bounds = f.getStringBounds(message, context);

        //字符宽度＝字符串长度/字符数
        double char_interval = (bounds.getWidth() / ilength);
        //上坡度
        double ascent = -bounds.getY();

        int first = 0, second = 0;
        boolean odd = false;
        if (ilength % 2 == 1) {
            first = (ilength - 1) / 2;
            odd = true;
        } else {
            first = (ilength) / 2 - 1;
            second = (ilength) / 2;
            odd = false;
        }

        double radius2 = radius - ascent;
        double x0 = CENTERX;
        double y0 = CENTERY - radius + ascent;
        //旋转角度
        double a = 2 * Math.asin(char_interval / (2 * radius2));

        if (odd) {
            g.setFont(f);
            g.drawString(messages[first], (float) (x0 - char_interval / 2), (float) y0);

            //中心点的右边
            for (int i = first + 1; i < ilength; i++) {
                double aa = (i - first) * a;
                double ax = radius2 * Math.sin(aa);
                double ay = radius2 - radius2 * Math.cos(aa);
                AffineTransform transform = AffineTransform.getRotateInstance(aa);//,x0 + ax, y0 + ay);
                Font f2 = f.deriveFont(transform);
                g.setFont(f2);
                g.drawString(messages[i], (float) (x0 + ax - char_interval / 2 * Math.cos(aa)), (float) (y0 + ay - char_interval / 2 * Math.sin(aa)));
            }
            //中心点的左边
            for (int i = first - 1; i > -1; i--) {
                double aa = (first - i) * a;
                double ax = radius2 * Math.sin(aa);
                double ay = radius2 - radius2 * Math.cos(aa);
                AffineTransform transform = AffineTransform.getRotateInstance(-aa);//,x0 + ax, y0 + ay);
                Font f2 = f.deriveFont(transform);
                g.setFont(f2);
                g.drawString(messages[i], (float) (x0 - ax - char_interval / 2 * Math.cos(aa)), (float) (y0 + ay + char_interval / 2 * Math.sin(aa)));
            }

        } else {
            //中心点的右边
            for (int i = second; i < ilength; i++) {
                double aa = (i - second + 0.5) * a;
                double ax = radius2 * Math.sin(aa);
                double ay = radius2 - radius2 * Math.cos(aa);
                AffineTransform transform = AffineTransform.getRotateInstance(aa);//,x0 + ax, y0 + ay);
                Font f2 = f.deriveFont(transform);
                g.setFont(f2);
                g.drawString(messages[i], (float) (x0 + ax - char_interval / 2 * Math.cos(aa)), (float) (y0 + ay - char_interval / 2 * Math.sin(aa)));
            }

            //中心点的左边
            for (int i = first; i > -1; i--) {
                double aa = (first - i + 0.5) * a;
                double ax = radius2 * Math.sin(aa);
                double ay = radius2 - radius2 * Math.cos(aa);
                AffineTransform transform = AffineTransform.getRotateInstance(-aa);//,x0 + ax, y0 + ay);
                Font f2 = f.deriveFont(transform);
                g.setFont(f2);
                g.drawString(messages[i], (float) (x0 - ax - char_interval / 2 * Math.cos(aa)), (float) (y0 + ay + char_interval / 2 * Math.sin(aa)));
            }
        }

        return buffImg;
    }



    /**
     * 方形名字章
     *
     * @param message 要刻的名字
     * @return
     */

    public static BufferedImage getSquarePng(String message) {
        BufferedImage image = new BufferedImage(180, 180, BufferedImage.TYPE_INT_ARGB);
        //得到图形上下文
        Graphics2D g2 = image.createGraphics();
        // 设置画笔颜色
        g2.setColor(Color.RED);
        // 设置字体
        g2.setFont(new Font("宋体", Font.LAYOUT_LEFT_TO_RIGHT, 80));
        //写内容
        if (message != null) {
            String newMessage = message;
            char[] charArray = newMessage.toCharArray();
            g2.drawString(new String(new char[]{charArray[0]}), 90, 70);
            g2.drawString(new String(new char[]{charArray[1]}), 90, 160);
            if (charArray.length == 3) {
                g2.drawString(new String(new char[]{charArray[2]}), 10, 115);
            } else {
                g2.drawString(new String(new char[]{charArray[3]}), 10, 160);
                g2.drawString(new String(new char[]{charArray[2]}), 10, 70);
            }
        }
        return image;
    }
}
