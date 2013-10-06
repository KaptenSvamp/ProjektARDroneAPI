package TagAlignment;

import ImageAnalysis.ImageAnalyser;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.navdata.AltitudeListener;
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
            public void receivedExtendedAltitude(
                            de.yadrone.base.navdata.Altitude d) {

            }

        };
        drone.getNavDataManager().addAltitudeListener(altitudeListener);
    }

    @Override
    public void imageUpdated(BufferedImage bi) {
        ImageAnalyser.analyse(bi);
    }
    
    public void ControlLoop()
    {
        try
        {
            while(!Thread.interrupted())
            {
                boolean foundColors = ImageAnalyser.foundColors();

                if(foundColors)
                {
                    boolean isCentered = false;
                    
                    synchronized(ImageAnalyser)
                    {
                        isCentered = ImageAnalyser.isCentered();
                    }
                    
                    if (!isCentered) // tag visible, but not centered
                    {
                        centerTag();
                    }                    
                    else if(DoLanding)
                    {
                        landing();
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("TagAlignment error: " + e);
        }
        
        Drone.getNavDataManager().removeAltitudeListener(altitudeListener);
    }
    
    private void centerTag() throws InterruptedException
    {
            Point points;
            double orientation;

            synchronized(ImageAnalyser)
            {
                points = ImageAnalyser.getOrigin();
                orientation = ImageAnalyser.getAngle();
            }

            int imgCenterX = TagAlignment.IMAGE_WIDTH / 2;
            int imgCenterY = TagAlignment.IMAGE_HEIGHT / 2;

            float x = points.x;
            float y = points.y;

            if ((orientation > 10) && (orientation < 180))
            {
                System.out.println("PaperChaseAutoController: Spin left");
                Drone.getCommandManager().spinLeft(SPEED * 2);
                Thread.currentThread().sleep(SLEEP);
            }
            else if ((orientation < 350) && (orientation > 180))
            {
                System.out.println("PaperChaseAutoController: Spin right");
                Drone.getCommandManager().spinRight(SPEED * 2);
                Thread.currentThread().sleep(SLEEP);
            }
            else if (x < (imgCenterX - TagAlignment.TOLERANCE))
            {
                System.out.println("PaperChaseAutoController: Go left");
                Drone.getCommandManager().goLeft(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else if (x > (imgCenterX + TagAlignment.TOLERANCE))
            {
                System.out.println("PaperChaseAutoController: Go right");
                Drone.getCommandManager().goRight(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else if (y < (imgCenterY - TagAlignment.TOLERANCE))
            {
                System.out.println("PaperChaseAutoController: Go forward");
                Drone.getCommandManager().forward(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else if (y > (imgCenterY + TagAlignment.TOLERANCE))
            {
                System.out.println("PaperChaseAutoController: Go backward");
                Drone.getCommandManager().backward(SPEED);
                Thread.currentThread().sleep(SLEEP);
            }
            else
            {
                System.out.println("TagAlignmentAutoController: Tag centered");
                Drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 5);
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