package com.rush.cloud.betslip.builder;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.rush.cloud.betslip.common.Position;
import com.rush.cloud.betslip.common.XAlignment;
import com.rush.cloud.betslip.common.YAlignment;

public class BufferedImageBuilder {

    private final BufferedImage img;

    public BufferedImageBuilder(int width, int height, int imageType) {
        this.img = new BufferedImage(width, height, imageType);
    }

    public BufferedImageBuilder addImage(URL imageUrl, int width, int height, int x, int y) {
        return addImage(imageUrl, width, height, x, y, 0);
    }

    public BufferedImageBuilder addImage(URL imageUrl, int width, int height, int x, int y, int cornerRadius) {
        return   addImage(imageUrl,width, height,x, y,cornerRadius, 0);
    }

    public BufferedImageBuilder addImage(URL imageUrl, int width, int height, int x, int y, int cornerRadius, int radians) {

        try {
            ImageIO.setUseCache(false);
            BufferedImage tmp = ImageIO.read(imageUrl);
            BufferedImage image = new BufferedImage(width, height,  BufferedImage.TYPE_INT_ARGB);
            Graphics2D newG2d = image.createGraphics();
            newG2d.drawImage(tmp, 0, 0, null);
            newG2d.dispose();

            if (cornerRadius > 0) {
                image = makeRoundedCorner(image, cornerRadius);
            }

            if (radians > 0) {
                image = rotateImage(image, radians);
            }

            Graphics2D g2d = this.img.createGraphics();
            g2d.drawImage(image, x, y, null);
            g2d.dispose();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: Handle exception
        }
        return this;
    }

    public BufferedImageBuilder addText(String text, XAlignment xAlignment, int xPadding, YAlignment yAlignment, int yPadding, Font font, Color fontColor) {
        return addText(text, xAlignment, xPadding, yAlignment, yPadding, font, fontColor, 0, 0);
    }

    public BufferedImageBuilder addText(String text, XAlignment xAlignment, int xPadding, YAlignment yAlignment, int yPadding, Font font, Color fontColor, int xManualAdjust, int yManualAdjust) {

        Graphics2D g2d = this.img.createGraphics();
        g2d.setPaint(fontColor);
        g2d.setFont(font);

        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();

        int x = 0;
        switch (xAlignment) {
            case LEFT:
                x = xPadding;
                break;
            case RIGHT:
                x = img.getWidth() - textWidth - xPadding;
                break;
            case CENTER:
                x = ((img.getWidth() - textWidth) / 2) + xManualAdjust;
                break;
            default:
        }

        int y = 0;
        switch (yAlignment) {
            case TOP:
                y = yPadding + fontMetrics.getAscent();
                break;
            case BOTTOM: // bottom padding not working as expected. specifying 0 does not put the text right above the bottom border
                y = (img.getHeight() - textHeight) + fontMetrics.getAscent() - yPadding;
                break;
            case CENTER:
                y = ((img.getHeight() - textHeight) / 2) + fontMetrics.getAscent() + fontMetrics.getLeading() + yManualAdjust;
                break;
            default:
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawString(text, x, y);
        g2d.dispose();

        return this;
    }

    public BufferedImageBuilder addLine(float thickness, Color color, boolean isDashed, int x1, int y1, int x2, int y2) {
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.setStroke(getStroke(thickness, isDashed));
        g2d.drawLine(x1, y1, x2, y2);
        g2d.dispose();

        return this;
    }

    public BufferedImageBuilder addBullet(float thickness, Color borderColor, Color fillColor, int width, int height, int x, int y) {
        Graphics2D g2d = img.createGraphics();
        g2d.setStroke(getStroke(thickness, false));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(borderColor);
        g2d.fillOval((int) (x-(thickness * 0.5)), (int) (y-(thickness * 0.5)), width + (int) thickness, height + (int) thickness); // fill any space before add the point on middle

        g2d.setColor(fillColor);
        g2d.fillOval((int) (x+(thickness * 0.5)), (int) (y+(thickness * 0.5)), width - (int) thickness , height - (int) thickness ); // fill any space before add the point on middle
        g2d.dispose();

        return this;
    }

    public BufferedImageBuilder addBorder(float thickness, Color color, boolean isDashed) {
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.setStroke(getStroke(thickness, isDashed));
        g2d.drawRect(0, 0, img.getWidth() - (int) thickness, img.getHeight() - (int) thickness);
        g2d.dispose();

        return this;
    }

    public BufferedImageBuilder addOverlay(Color color, Composite composite) {
        return  addOverlay(color, composite, 0, null);
    }

    public BufferedImageBuilder addOverlay(Color color, Composite composite, int cornerRadius, Position cornerPosition) {
        BufferedImage overlay = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        if (composite != null) {
            g2d.setComposite(composite);
        }

        if (cornerRadius > 0 ) {
            // Fill the position corners to just show rounded where be necessary
            switch (cornerPosition) {
                case TOP:
                    g2d.fillRect(0, img.getHeight() - (cornerRadius / 2 ), img.getWidth(), cornerRadius / 2 );
                    break;
                case RIGHT:
                    g2d.fillRect(0, 0, cornerRadius / 2, img.getHeight());
                    break;
                case BOTTOM:
                    g2d.fillRect(0, 0, img.getWidth(), cornerRadius / 2);
                    break;
                case LEFT:
                    g2d.fillRect(img.getWidth() - (cornerRadius / 2), 0, cornerRadius /2, img.getHeight());
                    break;
            }
            g2d.fillRoundRect(0, 0, img.getWidth(), img.getHeight(), cornerRadius, cornerRadius);
        } else {
            g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
        }
        g2d.drawImage(overlay, 0, 0, null);

        g2d.dispose();

        return this;
    }

    public BufferedImageBuilder addSection(BufferedImage imgSection, int width, int height, int x, int y) {

        Graphics2D g2d = img.createGraphics();
        g2d.drawImage(imgSection, x, y, null);
        g2d.dispose();

        return this;
    }

    public BufferedImageBuilder addRectangle(Color color, int x1, int y1, int width, int height, int xPadding, int yPadding) {
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(x1+ xPadding, y1+yPadding, width, height);
        g2d.dispose();

        return this;
    }

    public BufferedImageBuilder addRoundedSkewedOverlay(Color color, int arc, double shx, double shy) {

        Graphics2D g2d = img.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.shear(shx, shy);
        g2d.fill(new RoundRectangle2D.Float(20, 0, img.getWidth() - 20, img.getHeight(), arc, arc));

        g2d.setComposite(AlphaComposite.SrcAtop);
        g2d.drawImage(img, 0, 0, null);

        g2d.dispose();

        return this;
    }

    private BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {

        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        output = g2.getDeviceConfiguration().createCompatibleImage(image.getWidth(), image.getHeight(), Transparency.TRANSLUCENT);
        g2.dispose();

        g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.fillRoundRect(0, 0, image.getWidth(), image.getHeight(), cornerRadius, cornerRadius);
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        g2.dispose();

        return output;
    }

    public BufferedImage build() {
        return this.img;
    }

    //TODO: this method is very slow
    private BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image resultingImage = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage newImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        newImg.getGraphics().drawImage(resultingImage, 0, 0, null);

        return newImg;
    }

    private Stroke getStroke(float thickness, boolean isDashed) {
        if (isDashed) {
            return new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
        } else {
            return new BasicStroke(thickness);
        }
    }

    private BufferedImage rotateImage(BufferedImage imageToRotate, int radians) {
        int widthOfImage = imageToRotate.getWidth();
        int heightOfImage = imageToRotate.getHeight();
        int typeOfImage = imageToRotate.getType();

        BufferedImage newImageFromBuffer = new BufferedImage(widthOfImage, heightOfImage, typeOfImage);

        Graphics2D graphics2D = newImageFromBuffer.createGraphics();

        graphics2D.rotate(Math.toRadians(radians), widthOfImage / 2, heightOfImage / 2);
        graphics2D.drawImage(imageToRotate, null, 0, 0);

        return newImageFromBuffer;
    }

}
