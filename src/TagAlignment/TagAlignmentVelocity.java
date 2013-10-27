/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TagAlignment;

import NavigationData.NavdataLogger;
import de.yadrone.base.ARDrone;
import de.yadrone.base.navdata.VelocityListener;
import java.util.concurrent.ConcurrentLinkedDeque;


public class TagAlignmentVelocity implements VelocityListener{

    private ARDrone Drone;
    
    private ConcurrentLinkedDeque<VelocitySample> VelocityXSamples; 
    private ConcurrentLinkedDeque<VelocitySample> VelocityYSamples; 
    
    //private ConcurrentLinkedDeque<VelocitySample> VelocityXSamplesFiltered; 
    //private ConcurrentLinkedDeque<VelocitySample> VelocityYSamplesFiltered;
    
    private final int NrOfSamples = 10;
    private long LastVelocityReadTime;
    
    private DistanceTraveled DistanceX;
    private DistanceTraveled DistanceY;
    
    private VelocitySample VelocityX;
    private VelocitySample VelocityY;
    
    private NavdataLogger logger;
    
    private long SystemStartTime;
    public long GetTimeSinceStart(){return System.currentTimeMillis() - SystemStartTime;}
    
    //private float CurrentYaw;
    //private float ReferenceYaw;
    //public synchronized void setReferenceYaw(float referenceYaw){ ReferenceYaw = referenceYaw;}
    
    public TagAlignmentVelocity(ARDrone drone)
    {
        SystemStartTime = System.currentTimeMillis();
        
        Drone = drone;
        
        logger = new NavdataLogger("TagAlignmentVelocity", 
                "MavVX" + NavdataLogger.sep
                + "MavVY" + NavdataLogger.sep
                + "DistanceX" + NavdataLogger.sep
                + "DistanceY" + NavdataLogger.sep);
        
        VelocityXSamples = new ConcurrentLinkedDeque<VelocitySample>();
        VelocityYSamples = new ConcurrentLinkedDeque<VelocitySample>();
        
        //VelocityXSamplesFiltered = new ConcurrentLinkedDeque<VelocitySample>();
        //VelocityYSamplesFiltered = new ConcurrentLinkedDeque<VelocitySample>();
        
        DistanceX = new DistanceTraveled();
        DistanceY = new DistanceTraveled();
        
        VelocityX = new VelocitySample();
        VelocityY = new VelocitySample();
        
        LastVelocityReadTime = System.currentTimeMillis();
    }
    
    @Override
    public synchronized void velocityChanged(float vx, float vy, float vz) {
        
        long temp = System.currentTimeMillis();
        long timeStamp = LastVelocityReadTime - temp;
        LastVelocityReadTime = temp;
        
        if(VelocityXSamples.size() >= NrOfSamples)
        {
            VelocityXSamples.removeLast();
        }
        VelocityXSamples.addFirst(new VelocitySample(vx, timeStamp));
        
        if(VelocityYSamples.size() >= NrOfSamples)
        {
            VelocityYSamples.removeLast();
        }
        VelocityYSamples.addFirst(new VelocitySample(vy, timeStamp));
        
        /* -- Moving average X -- */
        int size = VelocityXSamples.size();
        float sumX = 0;
        for(VelocitySample sample : VelocityXSamples)
        {
            sumX += sample.getSample();
        }
        
        float mavX = sumX/size;
        VelocityX.setSample(mavX);
        VelocityX.setTimeStamp(timeStamp);
        //VelocityXSamplesFiltered.addFirst(new VelocitySample(mavX, timeStamp));
        
        /* -- Moving average Y -- */
        size = VelocityYSamples.size();
        float sumY = 0;
        for(VelocitySample sample : VelocityXSamples)
        {
            sumY += sample.getSample();
        }
        
        float mavY = sumY/size;
        VelocityY.setSample(mavY);
        VelocityY.setTimeStamp(timeStamp);
        //VelocityYSamplesFiltered.addFirst(new VelocitySample(mavY, timeStamp));
        
        /* -- Calculate & store distance -- */
        float distX = mavX*(timeStamp/1000);
        DistanceX.setDistance(distX);
        
        float distY = mavY*(timeStamp/1000);
        DistanceY.setDistance(distY);
        
        String[] loggerData = {mavX+"", mavY+"", distX+"", distY+""};
        
        logger.LogData(loggerData, GetTimeSinceStart(), timeStamp);
        
        System.out.println("VX: " + mavX + " VY: " + mavY + " distX: " + distX + " distY: " + distY);
    }
    
}
