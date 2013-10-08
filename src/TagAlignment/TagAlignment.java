package TagAlignment;

import ImageAnalysis.ImageAnalyser;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.navdata.AltitudeListener;
import de.yadrone.base.navdata.AttitudeListener;
import de.yadrone.base.video.ImageListener;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class TagAlignment implements ImageListener
{
    public final static int IMAGE_WIDTH = 640; // 640 or 1280
    public final static int IMAGE_HEIGHT = 360; // 360 or 720
    
    public final static int TOLERANCE = 100;
    private final static int SPEED = 5;
    private final static int SLEEP = 500;
    private final static int LandingAltitude = 200;	//mm
    
    private ImageAnalysis.ImageAnalyser ImageAnalyser;
    private final IARDrone Drone;
    
    private boolean DoLanding;
    public void SetLanding(boolean land){DoLanding = land;}
    
    private int Altitude; 				//mm
    private AltitudeListener altitudeListener;
    
    private float CurrentYaw;
    private AttitudeListener attitudeListener;
    
    private float ReferenceYaw;
    public synchronized void SetReferenceYaw(float yaw){ReferenceYaw = yaw; 
    System.out.println("RefSet: " + yaw/1000);}
    public void SetReferenceYaw(){ReferenceYaw = CurrentYaw; 
    System.out.println("RefSet to current ");}
    public float GetReferenceYaw(){return ReferenceYaw;}
    
    public TagAlignment(IARDrone drone)
    {
        Drone = drone;
        ImageAnalyser = new ImageAnalyser();
        
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

                public synchronized void attitudeUpdated(float pitch, float roll) { }
                public synchronized void windCompensation(float pitch, float roll) { }
        };
        
        drone.getNavDataManager().addAttitudeListener(attitudeListener);
    }

    @Override
    public void imageUpdated(BufferedImage bi) {
        //System.out.println("image recived");
        synchronized(ImageAnalyser)
        {
            ImageAnalyser.analyse(bi);
        }
    }
    
    public void ControlLoop()
    {
        try
        {
            while(!Thread.interrupted())
            {
                centerTagTesting();
                /*
                boolean foundColors = ImageAnalyser.foundColors();
                
                if(foundColors)
                {
                    boolean isCentered = false;
                   
                    isCentered = ImageAnalyser.isCentered() && IsOriented();
                    Point points = ImageAnalyser.getOrigin();

                    System.out.println("x: " + points.getX() + " y: " + points.getY());

                    if(isCentered)
                        System.out.println("CENTERED!!!!!!!!!!!!!!!");
                    
                    if (!isCentered) // tag visible, but not centered
                    {
                        //centerTag();
                    }                    
                    else if(DoLanding)
                    {
                        //landing();
                    }
                    
                    Thread.currentThread().sleep(SLEEP);
                }
                * */
            }
        }
        catch(Exception e)
        {
            System.out.println("TagAlignment error: " + e);
        }
        
        Drone.getNavDataManager().removeAltitudeListener(altitudeListener);
        Drone.getNavDataManager().removeAttitudeListener(attitudeListener);
    }
    
    private boolean IsOriented()
    {
        float orientation = (ReferenceYaw - CurrentYaw)/1000;
        
        return orientation < 10 && orientation > -10;
    }
    
    private void centerTagTesting() throws InterruptedException
    {
        float orientation = (ReferenceYaw - CurrentYaw)/1000;

        System.out.println("orientation: " + orientation + "ref: " + ReferenceYaw/1000 + " curr: " + CurrentYaw/1000);
        if ((orientation > 10) && (orientation < 180) || (orientation < -10) && (orientation < -180))
        {
            System.out.println("PaperChaseAutoController: Spin left");
            Drone.getCommandManager().spinLeft(SPEED * 2);
            Thread.currentThread().sleep(SLEEP);
        }
        else if ((orientation > 10) && (orientation > 180) || (orientation < -10) && (orientation > -180))
        {
            System.out.println("PaperChaseAutoController: Spin right");
            Drone.getCommandManager().spinRight(SPEED * 2);
            Thread.currentThread().sleep(SLEEP);
        }
        
    }
    
    private void centerTagMove() throws InterruptedException
    {
        int speedX = 0;
        int speedY = 0;
        int speedZ = 0;
        int speedSpin = 0;
        
        Point points;
        float orientation = (ReferenceYaw - CurrentYaw)/1000;

        System.out.println("orientation: " + orientation + " ref: " + ReferenceYaw/1000 + " curr: " + CurrentYaw/1000);

        synchronized(ImageAnalyser)
        {
            points = ImageAnalyser.getOrigin();
        }

        int imgCenterX = TagAlignment.IMAGE_WIDTH / 2;
        int imgCenterY = TagAlignment.IMAGE_HEIGHT / 2;

        double x = points.getX();
        double y = points.getY();

        // Spin left/right
        if ((orientation > 10) && (orientation < 180) || (orientation < -10) && (orientation < -180))
        {
            System.out.println("PaperChaseAutoController: Spin left");
            speedSpin = -(SPEED*2);
        }
        else if ((orientation > 10) && (orientation > 180) || (orientation < -10) && (orientation > -180))
        {
            System.out.println("PaperChaseAutoController: Spin right");
            speedSpin = SPEED*2;
        }
        
        // Go left/right
        if (x < (imgCenterX - TagAlignment.TOLERANCE))
        {
            System.out.println("PaperChaseAutoController: Go left");
            speedX = -SPEED;
        }
        else if (x > (imgCenterX + TagAlignment.TOLERANCE))
        {
            System.out.println("PaperChaseAutoController: Go right");
            speedX = SPEED;
        }
        
        // Go forward/backward
        if (y < (imgCenterY - TagAlignment.TOLERANCE))
        {
            System.out.println("PaperChaseAutoController: Go forward");
            speedY = SPEED;
        }
        else if (y > (imgCenterY + TagAlignment.TOLERANCE))
        {
            System.out.println("PaperChaseAutoController: Go backward");
            speedY = -SPEED;
        }
        
        if(speedX != 0 || speedY != 0 || speedZ != 0 || speedSpin != 0)
        {
            Drone.getCommandManager().move(speedX, speedY, speedZ, speedSpin);
            Thread.sleep(SLEEP);
        }
        else
        {
            System.out.println("TagAlignmentAutoController: Tag centered");
            //Drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 5);
        }
    }
    
    private void centerTag() throws InterruptedException
    {
            Point points;
            float orientation = (ReferenceYaw - CurrentYaw)/1000;

            System.out.println("orientation: " + orientation + " ref: " + ReferenceYaw/1000 + " curr: " + CurrentYaw/1000);
            
            synchronized(ImageAnalyser)
            {
                points = ImageAnalyser.getOrigin();
            }

            int imgCenterX = TagAlignment.IMAGE_WIDTH / 2;
            int imgCenterY = TagAlignment.IMAGE_HEIGHT / 2;

            double x = points.getX();
            double y = points.getY();

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
            else if (x < (imgCenterX - TagAlignment.TOLERANCE))
            {
                System.out.println("PaperChaseAutoController: Go left");
                //Drone.getCommandManager().goLeft(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else if (x > (imgCenterX + TagAlignment.TOLERANCE))
            {
                System.out.println("PaperChaseAutoController: Go right");
                //Drone.getCommandManager().goRight(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else if (y < (imgCenterY - TagAlignment.TOLERANCE))
            {
                System.out.println("PaperChaseAutoController: Go forward");
                //Drone.getCommandManager().forward(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else if (y > (imgCenterY + TagAlignment.TOLERANCE))
            {
                System.out.println("PaperChaseAutoController: Go backward");
                //Drone.getCommandManager().backward(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else
            {
                System.out.println("TagAlignmentAutoController: Tag centered");
                //Drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 5);
            }
	}
        
        private void landing() throws InterruptedException
	{
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
                Drone.getCommandManager().down(30);
                Drone.getCommandManager().setMaxAltitude(Altitude);
                Thread.currentThread().sleep(SLEEP);
            }
	}	
}

/*
public class TagAlignment
{
    public final static int IMAGE_WIDTH = 640; // 640 or 1280
    public final static int IMAGE_HEIGHT = 360; // 360 or 720

    public final static int TOLERANCE = 100;

    private IARDrone drone = null;
    private TagAlignmentAutoController autoController;
    private QRCodeScanner scanner = null;
    private PaperChaseGUI gui = null;
    
    private boolean Enabled;
    public boolean IsEnabled(){return Enabled;}
    private boolean Debug;

    public TagAlignment(IARDrone drone)
    {
        this(drone, false);
    }
    
    public TagAlignment(IARDrone drone, boolean debug)
    {
            this.drone = drone;
            Debug = debug;
    }

    public void landOnTag()
    {
            if(!Enabled)
                    enableAutoControl(true);

            autoController.landOnTag();
    }
    
    public void abortLanding()
    {
            if(autoController != null)
                    autoController.abortLanding();
    }

    public void enableAutoControl(boolean enable)
    {
        Enabled = enable;

        if (enable)
        {
            this.drone.getCommandManager().setVideoChannel(VideoChannel.VERT);

            scanner = new QRCodeScanner();
            
            if(Debug)
            {
                gui = new PaperChaseGUI(drone);
                scanner.addListener(gui);
                drone.getVideoManager().addImageListener(gui);
            }

            // auto controller is instantiated, but not started
            autoController = new TagAlignmentAutoController(drone);

            this.drone.getVideoManager().addImageListener(scanner);

            scanner.addListener(autoController);
            //autoController.start();
        }
        else
        {
            //autoController.abortLanding();
            autoController.stopController();
            
            if(Debug)
            {
                gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
                this.drone.getVideoManager().removeImageListener(gui);
            }
            //this.drone.getVideoManager().removeImageListener(imgTagListener);

            scanner.removeListener(autoController); // only auto autoController registers as TagListener
        }
    }
}
*/