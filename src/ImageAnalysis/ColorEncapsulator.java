/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAnalysis;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 *
 * @author Victor
 */
public class ColorEncapsulator {
    int xmin,xmax,ymin,ymax;
    private COLORS color;
    private boolean foundColor;
    private final int dominans = 90;
    /**
     * Creates a ColorEncapsulator class
     * @param c Specified color to look for
     * @param w The image width
     * @param h The image height 
     */
    public ColorEncapsulator(COLORS c, int w, int h)
    {
        this.color = c;
        foundColor = false;
        xmin=w;
        xmax=0;
        ymin=h;
        ymax=0;
    }
    
    /**
     * Checks wheter a specified coordinate is of the specified color or not and saves the coordinates for future usage
     * @param x
     * @param y
     * @param rgb 
     */
    public void trackColor(int x, int y, int rgb)
    {
        if (isDominant(this.color, rgb))
        {
            xmin = (xmin>x) ? x : xmin;
            xmax = (xmax<x) ? x : xmax;
            
            ymin = (ymin>y) ? y : ymin;
            ymax = (ymax<y) ? y : ymax;
            foundColor = true;
        }
    }
    
    /**
     * Checks wheter the specified color is dominant in comparison with the rgb-value
     * @param c
     * @param rgb
     * @return 
     */
    private boolean isDominant(COLORS c, int rgb)
    {
        int red     = (rgb & 0x00ff0000) >> 16;
        int green   = (rgb & 0x0000ff00) >> 8;
        int blue    = (rgb & 0x000000ff);
        switch (c)
        {
            case RED:
//                if (red > 2* blue && red > 2*green)
                if (red > dominans && blue < 60 && green < 60)
                    return true;
                return false;
            case GREEN:
                if (red < 100 && blue < 100 && green > dominans)
//                if (green > 2* blue && green > 2*red)
                    return true;
                return false;
            case BLUE:
                if (red < 100 && blue > dominans && green < 100)
//                if (green > 2* blue && green > 2*red)
                    return true;
                return false;
            case WHITE:
                if (red > 240 && blue > 240 && green > 240)
//                if (green > 2* blue && green > 2*red)
                    return true;
                return false;

            default:
                return false;
        }
    }
    
    /**
     * Draws the capsule into the given BufferedImage
     * @param bi 
     */
    public void drawCapsule(BufferedImage bi)
    {
        if (colorFound())
        {
            for (int i = xmin;i<xmax;i++)
            {
                bi.setRGB(i, ymin, 0xffffff);
                bi.setRGB(i, ymax, 0xffffff);
            }
            for (int i = ymin;i<ymax;i++)
            {
                bi.setRGB(xmin, i, 0xffffff);
                bi.setRGB(xmax, i, 0xffffff);
            }
        }
    }
    
    /**
     * Returns the origin of the capsule
     * @return 
     */
    public Point getPosition()
    {
        return new Point(xmin + ((xmax-xmin) / 2),ymin + ((ymax-ymin) / 2));
    }
    
    /**
     * Determines wheter the specified color was found or not
     * @return 
     */
    public boolean colorFound()
    {
        return foundColor;
    }
    /**
     * Returns the positions of the capsule
     * @return [xmin, xmax, ymin, ymax]
     */
    private int[] getCapsule()
    {
        int[] retVal = {xmin,xmax,ymin,ymax};
        return retVal;
    }
}
