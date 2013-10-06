package TagAlignment;

import de.yadrone.base.IARDrone;
import de.yadrone.base.command.VideoChannel;
import de.yadrone.apps.paperchase.QRCodeScanner;
import de.yadrone.apps.paperchase.PaperChaseGUI;
import de.yadrone.base.video.ImageListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class TagAlignment implements ImageListener
{
    public final static int IMAGE_WIDTH = 640; // 640 or 1280
    public final static int IMAGE_HEIGHT = 360; // 360 or 720

    public final static int TOLERANCE = 100;
    
    
    
    public TagAlignment(IARDrone drone)
    {
        
    }

    @Override
    public void imageUpdated(BufferedImage bi) {
        
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