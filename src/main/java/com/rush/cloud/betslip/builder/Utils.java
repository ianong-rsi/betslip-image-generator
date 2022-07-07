package com.rush.cloud.betslip.builder;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

public class Utils {
    public static <T> T logMethodExecTime(Supplier<T> r, String process) {
        long start = System.nanoTime();
        T returnVal = r.get();
        long end = System.nanoTime();

        long duration = (end - start) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("[DURATION] - " + process + " - " + duration);
        return returnVal;
    }

    public static int getTextWidth(BufferedImage img,String text, Font font){
        Graphics2D g2d = img.createGraphics();
        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        return fontMetrics.stringWidth(text);
    }

    public static int getTextHeight(BufferedImage img, String text, Font font){
        Graphics2D g2d = img.createGraphics();
        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        return fontMetrics.getHeight();
    }
}
