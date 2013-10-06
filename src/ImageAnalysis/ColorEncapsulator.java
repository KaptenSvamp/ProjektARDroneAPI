/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAnalysis;

import java.awt.Color;
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
    private final int dominans = 150;
    public ColorEncapsulator(COLORS c, int w, int h)
    {
        this.color = c;
        foundColor = false;
        xmin=w;
        xmax=0;
        ymin=h;
        ymax=0;
    }
    
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
    
    private boolean isDominant(COLORS c, int rgb)
    {
        int red     = (rgb & 0x00ff0000) >> 16;
        int green   = (rgb & 0x0000ff00) >> 8;
        int blue    = (rgb & 0x000000ff);
        switch (c)
        {
            case RED:
//                if (red > 2* blue && red > 2*green)
                if (red > dominans && blue < 50 && green < 50)
                    return true;
                return false;
            case GREEN:
                if (red < 100 && blue < 100 && green > dominans)
//                if (green > 2* blue && green > 2*red)
                    return true;
                return false;
            default:
                return false;
        }
    }
    
    public void drawCapsule(BufferedImage bi)
    {
        if (colorFound())
        {
            for (int i = xmin;i<xmax;i++)
            {
                bi.setRGB(i, ymin, 0);
                bi.setRGB(i, ymax, 0);
            }
            for (int i = ymin;i<ymax;i++)
            {
                bi.setRGB(xmin, i, 0);
                bi.setRGB(xmax, i, 0);
            }
        }
    }
    
    public Point getPosition()
    {
        return new Point(xmin + ((xmax-xmin) / 2),ymin + ((ymax-ymin) / 2));
    }
    
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
