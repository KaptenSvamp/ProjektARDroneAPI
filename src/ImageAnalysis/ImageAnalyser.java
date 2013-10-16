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
    private Point irFinderPoint;
    private BufferedImage currentImage;
     /**
     * @return the currentImage
     */
    public BufferedImage getCurrentImage() {return currentImage;}
    /**
     * Creates a new ImageAnalyser
     */
    public ImageAnalyser()            
    {
        currentImage = null;
        irFinderPoint = null;
    }
    /**
     * Analyses the passed BufferedImage - e.g. looks for colors
     * @param bi The image to process
     * @return 
     */
    public BufferedImage analyse(BufferedImage bi)
    {
        this.currentImage = bi;
        ColorEncapsulator IR_Finder = new ColorEncapsulator(COLORS.RED, currentImage.getWidth(), currentImage.getHeight());
        irFinderPoint = null;

        for (int x=0;x<this.currentImage.getWidth();x+=5)
        {
            for (int y=0;y<this.currentImage.getHeight();y+=5)
            {
                IR_Finder.trackColor(x, y, currentImage.getRGB(x, y));
            }
        }
        IR_Finder.drawCapsule(currentImage);
        if (IR_Finder.colorFound())
        {
            irFinderPoint = IR_Finder.getPosition();
        }
        return this.currentImage;
    }
    /**
     * Determines if both red and green was identified or not
     * @return 
     */
    public boolean foundColors()
    {
        return foundIR();
    }
    /**
     * Determines if the red blob was found or not
     * @return 
     */
    public boolean foundIR()
    {
        return (irFinderPoint != null);
    }

    /**
     * Determines if the identified body is centerered or not.
     * @return 
     */    
    /*public boolean isCentered()
    {
        int tolerance = TagAlignment.TagAlignment.TOLERANCE / 2;
        //int w = TagAlignment.TagAlignment.IMAGE_WIDTH;
        //int h = TagAlignment.TagAlignment.IMAGE_HEIGHT;
        int w = TagAlignment.TagAlignment.IMAGE_WIDTH / 2;
        int h = TagAlignment.TagAlignment.IMAGE_HEIGHT / 2;
        boolean horizontal;
        boolean vertical;
        
        double x = getOrigin().getX();
        double y = getOrigin().getY();
        
        horizontal  = (x > w-tolerance && x < w+tolerance);
        vertical    = (y > h-tolerance && y < h+tolerance);
        
        return (horizontal && vertical);
    }*/
    /**
     * Returns the origin of the identified body
     * @return Origin in coordinates relative to the analysed BufferedImage
     */
    public Point getOrigin()
    {
        Point retVal = new Point(0,0);
        if (irFinderPoint != null)
        {
            retVal = this.irFinderPoint;
        }
        return retVal;
    }
    
    /**
     * Returns the location of the red LED.
     * @return Point X,Y
     */
    public Point getRedPoint(){return irFinderPoint;}
}