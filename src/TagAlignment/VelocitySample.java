/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TagAlignment;

/**
 *
 * @author Admin
 */
public class VelocitySample {
    private float Sample;
    public synchronized float getSample(){return Sample;}
    public synchronized void setSample(float sample){Sample = sample;}
    
    private long TimeStamp;
    public float getTimeStamp(){return TimeStamp;}
    public synchronized void setTimeStamp(long timestamp){TimeStamp = timestamp;}
    
    public VelocitySample()
    {}
    
    public VelocitySample(float sample, long timestamp)
    {
        Sample = sample;
        TimeStamp = timestamp;
    }
    
}
