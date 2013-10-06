/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAnalysis;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Victor
 */
public class ImageAnalyser  {
    private double dx;
    private double dy;
    private Point greenPoint;
    private Point redPoint;
    private int xmin=-1;
    private int xmax=-1;
    private int ymin=-1;
    private int ymax=-1;
    private BufferedImage currentImage;
     /**
     * @return the currentImage
     */
    public BufferedImage getCurrentImage() {return currentImage;}
    public ImageAnalyser()            
    {
        currentImage = null;
        greenPoint = null;
        redPoint = null;
    }
    
    public BufferedImage analyse(BufferedImage bi)
    {
        this.currentImage = bi;
        ColorEncapsulator red = new ColorEncapsulator(COLORS.RED, currentImage.getWidth(), currentImage.getHeight());
        ColorEncapsulator green = new ColorEncapsulator(COLORS.GREEN, currentImage.getWidth(), currentImage.getHeight());
        greenPoint = null;
        redPoint = null;
        xmin=this.getCurrentImage().getWidth()-1;
        xmax=0;
        ymin=this.getCurrentImage().getHeight()-1;
        ymax=0;
        for (int x=0;x<this.currentImage.getWidth();x+=5)
        {
            for (int y=0;y<this.currentImage.getHeight();y+=5)
            {
                red.trackColor(x, y, currentImage.getRGB(x, y));
                green.trackColor(x, y, currentImage.getRGB(x, y));
            }
        }
        red.drawCapsule(currentImage);
        green.drawCapsule(currentImage);
        if (red.colorFound() && green.colorFound())
        {
            greenPoint = green.getPosition();
            redPoint = red.getPosition();
        }
        return this.currentImage;
    }
    
    public boolean foundColors()
    {
        return (foundRed() && foundGreen());
    }
    
    public boolean foundRed()
    {
        return (redPoint != null);
    }

    public boolean foundGreen()
    {
        return (greenPoint != null);
    }
    
    public boolean isCentered()
    {
        return (oriented() && centered());
    }
    
    private boolean oriented()
    {
        return (getAngle() > 350 && getAngle() < 10);
    }
    
    private boolean centered()
    {
        int tolerance = TagAlignment.TagAlignment.TOLERANCE / 2;
        int w = TagAlignment.TagAlignment.IMAGE_WIDTH;
        int h = TagAlignment.TagAlignment.IMAGE_HEIGHT;
        boolean horizontal;
        boolean vertical;
        
        horizontal  = (getOrigin().x > w-tolerance && getOrigin().x < w+tolerance);
        vertical    = (getOrigin().y > h-tolerance && getOrigin().y < h+tolerance);
        
        return (horizontal && vertical);
    }
    
    public Point getOrigin()
    {
        Point retVal = new Point(0,0);
        if (greenPoint != null && redPoint != null)
        {
            int xmin = (greenPoint.x < redPoint.x) ? greenPoint.x : redPoint.x;
            int xmax = (greenPoint.x > redPoint.x) ? greenPoint.x : redPoint.x;
            int ymin = (greenPoint.y < redPoint.y) ? greenPoint.y : redPoint.y;
            int ymax = (greenPoint.y > redPoint.y) ? greenPoint.y : redPoint.y;

            retVal = new Point(xmin + ((xmax-xmin) / 2), ymin + ((ymax-ymin) / 2));
        }
        return retVal;
    }
    
    /**
     * Returns green LED location and red LED location
     * @return [0] green, [1] red
     */
    public Point[] getLocation()
    {
        Point[] retVal = new Point[2];
        retVal[0] = new Point(0,0);
        retVal[1] = new Point(0,0);
        if (greenPoint != null && redPoint != null)
        {
            retVal[0] = greenPoint;
            retVal[1] = redPoint;
        }
        return retVal;
    }
    
    public double getAngle()
    {
        double angle = -1.0;
        if (greenPoint != null && redPoint != null)
        {
            angle = (double) Math.toDegrees(Math.atan2(redPoint.x - greenPoint.x, redPoint.y - greenPoint.y));
            if (angle < 0)
                angle += 360;
        }
        return angle;
    }
    
    public int[] getPoints()
    {
        int[] retVal = {xmin,ymin,xmax,ymax};
        return retVal;
    }
    
    /*
    @Override
    public void imageUpdated(BufferedImage bi) {
        analyse(bi);
    }*/

}
