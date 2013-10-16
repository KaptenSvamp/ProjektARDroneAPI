package ImageAnalysis;

/**
 *
 * @author Rasmus Bjerstedt
 */
public class AnalysedImageObject {
    public long TimeStamp;
    public double X;
    public double Y;
    public double O;
    public boolean FoundColors;
    
    public AnalysedImageObject(boolean foundColors, double x, double y)
    {
        this(foundColors, x, y, 0);
    }
    
    public AnalysedImageObject(boolean foundColors, double x, double y, double oriantation)
    {
        FoundColors = foundColors;
        TimeStamp = System.currentTimeMillis();
        X = x;
        Y = y;
        O = oriantation;
    }
    
}
