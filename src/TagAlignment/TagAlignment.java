package TagAlignment;

import ImageAnalysis.AnalysedImageObject;
import ImageAnalysis.ImageAnalyser;
import static TagAlignment.CommandEnum.Left;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.navdata.AltitudeListener;
import de.yadrone.base.navdata.AttitudeListener;
import de.yadrone.base.video.ImageListener;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles aligning and landing over a "tag" recognized by ImageAnalyser.
 * Orientation in handled by setting ReferenceYaw.
 * 
 * @author Rasmus Bjerstedt
 */
public class TagAlignment implements ImageListener
{
    private final IARDrone Drone;
    
    /* Static settings for camera feed */
    public final static int IMAGE_WIDTH = 640; // 640 or 1280
    public final static int IMAGE_HEIGHT = 360; // 360 or 720
    
    /* Static settings for precision, speed and so on. */
    public final static int TOLERANCE = 80;
    private final static int SPEED = 1;
    private final static int SLEEP = 500;
    private final static int LandingAltitude = 200;	//mm
    
    private boolean DoLanding;
    public void SetLanding(boolean land){DoLanding = land;}
    
    private int Altitude; 				//mm
    private AltitudeListener altitudeListener;
    
    /* Getting and setting the reference YAW. */
    private float CurrentYaw;
    private AttitudeListener attitudeListener;
    private float ReferenceYaw;
    public synchronized void SetReferenceYaw(float yaw){ReferenceYaw = yaw; System.out.println("RefSet to " + yaw);}
    public void SetReferenceYaw(){ ReferenceYaw = CurrentYaw; System.out.println("RefSet to current");}
    public float GetReferenceYaw(){return ReferenceYaw;}
    
    /* List for all analysed image data */
    private ConcurrentLinkedDeque<AnalysedImageObject> Images; 
    
    /* Last known command - to be able to find your way back to the tag. */
    private CommandEnum LastKnownCommand; // used with FindTag
    private int[] LastKnownCommandMove; // used with FindTagMove
    
    public TagAlignment(IARDrone drone)
    {
        Drone = drone;
        Images = new ConcurrentLinkedDeque<AnalysedImageObject>();
        
        altitudeListener = new AltitudeListener()
        {
            @Override
            public synchronized void receivedAltitude(int altitude) {
                    Altitude = altitude;
            }

            @Override
            public synchronized void receivedExtendedAltitude(
                            de.yadrone.base.navdata.Altitude d) {

            }

        };
        drone.getNavDataManager().addAltitudeListener(altitudeListener);
        
        /* Listener for setting ReferenceYaw */
        attitudeListener = new AttitudeListener() {

                @Override
                public synchronized void attitudeUpdated(float pitch, float roll, float yaw)
                {
                    CurrentYaw = yaw;
                }

                @Override
                public synchronized void attitudeUpdated(float pitch, float roll) { }
                @Override
                public synchronized void windCompensation(float pitch, float roll) { }
        };
        
        drone.getNavDataManager().addAttitudeListener(attitudeListener);
    }

    @Override
    public void imageUpdated(BufferedImage bi) {
        
        ImageAnalyser analyser = new ImageAnalyser();
        analyser.analyse(bi);
        
        Images.addFirst(new AnalysedImageObject(analyser.foundColors(), 
                analyser.getOrigin().getX(), analyser.getOrigin().getY()));
    }
    
    /*
     * Loop for checking, centering and landing helicopter over tag.
     */
    public void ControlLoop()
    {
        try
        {
            while(!Thread.interrupted())
            {
                AnalysedImageObject imageObject = Images.peek(); // alt. Images.pollFirst();
                
                if(imageObject == null)
                    continue;
                
                long time = 0;
                
                boolean recentImage = imageObject != null && 
                        (time = (System.currentTimeMillis() - imageObject.TimeStamp)) <= 500;
                
                //if(imageObject == null)
                   // System.out.println("Image is NULL");
                //else if(!recentImage)
                //    System.out.println("time out: " + time);
                
                
                if(recentImage && imageObject.FoundColors)
                {
                    boolean isCentered = false;
                   
                    isCentered = IsCentered(imageObject) && IsOriented();
                                        
                    if (!isCentered) // tag visible, but not centered
                    {
                        //centerTag(imageObject);
                        CenterTagMove(imageObject);
                    }                    
                    /*else if(DoLanding)
                    {
                        landing();
                    }*/
                    else{
                        Drone.getCommandManager().hover();
                        Thread.currentThread().sleep(SLEEP);
                        
                        if(DoLanding)
                        {
                            landing();
                        }
                    }
                }
                else
                {
                    //FindTag();
                    FindTagMove();
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("TagAlignmentLoop exception: " + e);
        }
        
        Drone.getNavDataManager().removeAltitudeListener(altitudeListener);
        Drone.getNavDataManager().removeAttitudeListener(attitudeListener);
    }
    
    /*
     * Indicates if the drone is oriented or not.
     */
    private boolean IsOriented()
    {
        float orientation = (ReferenceYaw - CurrentYaw)/1000;
        
        return orientation < 10 && orientation > -10;
    }
    
    /*
     * Indicates if the drone is centered (x,y) over the tag.
     */
    private boolean IsCentered(AnalysedImageObject imageObject)
    {
        int imgCenterX = TagAlignment.IMAGE_WIDTH / 2;
        int imgCenterY = TagAlignment.IMAGE_HEIGHT / 2;

        double x = imageObject.X;
        double y = imageObject.Y;
        
        return x > imgCenterX-TOLERANCE && x < imgCenterX+TOLERANCE
                && y > imgCenterY-TOLERANCE && y < imgCenterY+TOLERANCE;
    }
    
    private void centerTagTesting() throws InterruptedException
    {
        float orientation = (ReferenceYaw - CurrentYaw)/1000;

        System.out.println("orientation: " + orientation + "ref: " + ReferenceYaw/1000 + " curr: " + CurrentYaw/1000);
        if ((orientation > 10) && (orientation < 180) || (orientation < -10) && (orientation < -180))
        {
            System.out.println("PaperChaseAutoController: Spin left");
            //Drone.getCommandManager().spinLeft(SPEED * 2);
            Thread.currentThread().sleep(SLEEP);
        }
        else if ((orientation > 10) && (orientation > 180) || (orientation < -10) && (orientation > -180))
        {
            System.out.println("PaperChaseAutoController: Spin right");
            //Drone.getCommandManager().spinRight(SPEED * 2);
            Thread.currentThread().sleep(SLEEP);
        }
        
    }
    
    /*
     * Best control algorithm for centering drone. Moves drone in x, y, and YAW. 
     * Movement in Z not implemented.
     */
    private void CenterTagMove(AnalysedImageObject imageObject) throws InterruptedException
    {
        int speedX = 0;
        int speedY = 0;
        int speedZ = 0;
        int speedSpin = 0;
        
        Point points;
        float orientation = (ReferenceYaw - CurrentYaw)/1000;

        System.out.println("orientation: " + orientation + " ref: " + ReferenceYaw/1000 + " curr: " + CurrentYaw/1000);

        /*synchronized(ImageAnalyser)
        {
            points = ImageAnalyser.getOrigin();
        }*/

        int imgCenterX = TagAlignment.IMAGE_WIDTH / 2;
        int imgCenterY = TagAlignment.IMAGE_HEIGHT / 2;

        double x = imageObject.X;//points.getX();
        double y = imageObject.Y;//points.getY();
        
        // Go left/right
        if (x < (imgCenterX - TagAlignment.TOLERANCE))
        {
            System.out.println("TagAlignment: Go left");
            Drone.getCommandManager().goLeft(SPEED);
            speedX = -(SPEED);
        }
        else if (x > (imgCenterX + TagAlignment.TOLERANCE))
        {
            System.out.println("TagAlignment: Go right");
            Drone.getCommandManager().goRight(SPEED);
            speedX = (SPEED);
        }
        
        // Go forward/backward
        if (y < (imgCenterY - TagAlignment.TOLERANCE))
        {
            System.out.println("TagAlignment: Go forward");
            Drone.getCommandManager().forward(SPEED);
            speedY = (SPEED);
        }
        else if (y > (imgCenterY + TagAlignment.TOLERANCE))
        {
            System.out.println("TagAlignment: Go backward");
            Drone.getCommandManager().backward(SPEED);
            speedY = -(SPEED);
        }
        
        // Spin left/right
        if ((orientation > 10) && (orientation < 180) || (orientation < -10) && (orientation < -180))
        {
            System.out.println("TagAlignment: Spin left");
            speedSpin = -(SPEED*2);
            Drone.getCommandManager().spinLeft(SPEED*2);
        }
        else if ((orientation > 10) && (orientation > 180) || (orientation < -10) && (orientation > -180))
        {
            System.out.println("TagAlignment: Spin right");
            speedSpin = (SPEED*2);
            Drone.getCommandManager().spinRight(SPEED*2);
        }
        
        if(speedX != 0 || speedY != 0 || speedZ != 0 || speedSpin != 0)
        {
            LastKnownCommandMove = new int[]{speedX, speedY, speedZ, speedSpin};
            
            //Drone.getCommandManager().move(speedX, speedY, speedZ, speedSpin);
            Thread.sleep(SLEEP);
        }
        else
        {
            
            System.out.println("TagAlignmentAutoController: Tag centered");
        }
    }
    
    /*
     * Stupid algorithm for centering drone. Moves drone in ONE dimension at a time.
     * Z not implemented.
     */
    private void CenterTag(AnalysedImageObject imageObject) throws InterruptedException
    {
            Point points;
            float orientation = 0;//(ReferenceYaw - CurrentYaw)/1000;


            int imgCenterX = TagAlignment.IMAGE_WIDTH / 2;
            int imgCenterY = TagAlignment.IMAGE_HEIGHT / 2;

            double x = imageObject.X;//points.getX();
            double y = imageObject.Y;//points.getY();

            if (x < (imgCenterX - TagAlignment.TOLERANCE))
            {
                LastKnownCommand = CommandEnum.Left;
                System.out.println("PaperChaseAutoController: Go left");
                Drone.getCommandManager().goLeft(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else if (x > (imgCenterX + TagAlignment.TOLERANCE))
            {
                LastKnownCommand = CommandEnum.Right;
                System.out.println("PaperChaseAutoController: Go right");
                Drone.getCommandManager().goRight(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else if (y < (imgCenterY - TagAlignment.TOLERANCE))
            {
                LastKnownCommand = CommandEnum.Forward;
                System.out.println("PaperChaseAutoController: Go forward");
                Drone.getCommandManager().forward(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else if (y > (imgCenterY + TagAlignment.TOLERANCE))
            {
                LastKnownCommand = CommandEnum.Backward;
                System.out.println("PaperChaseAutoController: Go backward");
                Drone.getCommandManager().backward(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else if ((orientation > 10) && (orientation < 180) || (orientation < -10) && (orientation < -180))
            {
                LastKnownCommand = CommandEnum.SpinLeft;
                System.out.println("PaperChaseAutoController: Spin left");
                Drone.getCommandManager().spinLeft(SPEED * 2);
                Thread.currentThread().sleep(SLEEP);
            }
            else if ((orientation > 10) && (orientation > 180) || (orientation < -10) && (orientation > -180))
            {
                LastKnownCommand = CommandEnum.SpinRight;
                System.out.println("PaperChaseAutoController: Spin right");
                Drone.getCommandManager().spinRight(SPEED * 2);
                Thread.currentThread().sleep(SLEEP);
            }
            else
            {
                System.out.println("TagAlignmentAutoController: Tag centered");
                Drone.getCommandManager().hover();
                Thread.currentThread().sleep(SLEEP);
            }
	}
    
    /*
     * Finds tag from last known command. ONE command only. Z not implemented.
     * This method should be used with CenterTag.
     */
    private void FindTag() throws InterruptedException
    {
        switch(LastKnownCommand)
        {
            case Left:
            {
                System.out.println("Find tag: Go left");
                Drone.getCommandManager().goLeft(SPEED);
                Thread.currentThread().sleep(SLEEP);
                break;
            }
            case Right:
            {
                System.out.println("Find tag: Go right");
                Drone.getCommandManager().goRight(SPEED);
                Thread.currentThread().sleep(SLEEP);
                break;
            }
            case Forward:
            {
                System.out.println("Find tag: Go forward");
                Drone.getCommandManager().forward(SPEED);
                Thread.currentThread().sleep(SLEEP);
                break;
            }
            case Backward:
            {
                System.out.println("Find tag: Go backward");
                Drone.getCommandManager().backward(SPEED);
                Thread.currentThread().sleep(SLEEP);
                break;
            }
            case SpinLeft:
            {
                System.out.println("Find tag: Spin left");
                Drone.getCommandManager().spinLeft(SPEED * 2);
                Thread.currentThread().sleep(SLEEP);
                break;
            }
            case SpinRight:
            {
                System.out.println("Find tag: Spin right");
                Drone.getCommandManager().spinRight(SPEED * 2);
                Thread.currentThread().sleep(SLEEP);
                break;
            }
        }

    }
    
    /*
     * Finds tag from last known commands i X, Y and YAW. Z not implemented.
     * This method should be used with CenterTagMove.
     */
    private void FindTagMove() throws InterruptedException
    {
        if(LastKnownCommandMove.length > 0)
        {
            String right = LastKnownCommandMove[0] > 0 ? "go right " : "";
            String left = LastKnownCommandMove[0] < 0 ? "go left " : "";
            String forward = LastKnownCommandMove[1] > 0 ? "go forward " : "";
            String backward = LastKnownCommandMove[1] < 0 ? "go backward " : "";
            String spinRight = LastKnownCommandMove[3] > 0 ? "go spinRight " : "";
            String spinLeft = LastKnownCommandMove[3] < 0 ? "go spinLeft " : "";
            
            String all = right+left+forward+backward+spinRight+spinLeft;
            
            if(all.length() > 0)
            {
                all = "Find tag: " + all;
                System.out.println(all);
            }
            
            if(LastKnownCommandMove[0] > 0)
            {
                Drone.getCommandManager().goRight(SPEED*2);
                Thread.sleep(50);
            }
            else if(LastKnownCommandMove[0] < 0)
            {
                Drone.getCommandManager().goLeft(SPEED*2);
                Thread.sleep(50);
            }
            
            if(LastKnownCommandMove[1] > 0)
            {
                Drone.getCommandManager().forward(SPEED*2);
                Thread.sleep(50);
            }
            else if(LastKnownCommandMove[1] < 0)
            {
                Thread.sleep(50);
                Drone.getCommandManager().backward(SPEED*2);
            }
            
            /*if(LastKnownCommandMove[3] > 0)
                Drone.getCommandManager().spinRight(SPEED);
            else if(LastKnownCommandMove[3] < 0)
                Drone.getCommandManager().spinLeft(SPEED);
            */
            
            
            //Drone.getCommandManager().move(LastKnownCommandMove[0], LastKnownCommandMove[1], 
             //       LastKnownCommandMove[2], LastKnownCommandMove[3]);
            
            Thread.sleep(SLEEP);
        }
    }
    
    /*
     * Performs landing if altitude is low enough. Moves in Z otherwise.
     */
    private void landing() throws InterruptedException
    {
        System.out.println("TagAlignmentAutoController: Landing started...");
        Drone.getCommandManager().setLedsAnimation(LEDAnimation.GREEN, 10, 5);
        Drone.getCommandManager().landing();
        Thread.currentThread().sleep(5000);
        Thread.currentThread().interrupt();
        
        /*
        if(Altitude <= LandingAltitude)
        {
            System.out.println("TagAlignmentAutoController: Landing started...");
            Drone.getCommandManager().setLedsAnimation(LEDAnimation.GREEN, 10, 5);
            Drone.getCommandManager().landing();
            Thread.currentThread().sleep(5000);
            Thread.currentThread().interrupt();
        }
        else
        {
            System.out.println("TagAlignmentAutoController: Go Down");
            Drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_ORANGE, 10, 5);
            Drone.getCommandManager().down(20);
            //Drone.getCommandManager().setMaxAltitude(Altitude);
            Thread.currentThread().sleep(SLEEP);
        }
        */
    }	
}