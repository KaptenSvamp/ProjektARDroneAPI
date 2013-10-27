/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TagAlignment;

/**
 *
 * @author Admin
 */
public class DistanceTraveled {
    private float Distance;
    public synchronized float getDistance(){return Distance;}
    public synchronized void setDistance(float distance){Distance = distance;}
    
    public DistanceTraveled()
    {
        Distance = 0;
    }
    
}
