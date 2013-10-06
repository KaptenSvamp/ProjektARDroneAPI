package TagAlignment;

import java.util.ArrayList;
import java.util.Random;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.navdata.AltitudeListener;
import de.yadrone.base.navdata.ControlState;
import de.yadrone.base.navdata.DroneState;
import de.yadrone.base.navdata.StateListener;
import de.yadrone.apps.paperchase.TagListener;

public class TagAlignmentAutoController extends TagAlignmentAbstractController implements TagListener
{
	private final static int SPEED = 5;
	private final static int SLEEP = 500;
	private final static int LandingAltitude = 200;	//mm
	
	/* This list holds tag-IDs for all tags which have successfully been visited */
	private ArrayList<String> tagVisitedList = new ArrayList<String>();
	
	private Result tag;
	private float tagOrientation;
	
	private int Altitude; 				//mm
	private boolean IsFlying;
	
	private AltitudeListener altitudeListener;
	private StateListener stateListener;
	
	public TagAlignmentAutoController(IARDrone drone)
	{
		super(drone);
		
		altitudeListener = new AltitudeListener()
		{
			@Override
			public void receivedAltitude(int altitude) {
				Altitude = altitude;
			}

			@Override
			public void receivedExtendedAltitude(
					de.yadrone.base.navdata.Altitude d) {
				
			}
			
		};
		drone.getNavDataManager().addAltitudeListener(altitudeListener);
		
		stateListener = new StateListener() {
			
			public void stateChanged(DroneState state)
			{
				IsFlying = state.isFlying();
			}
			
			public void controlStateChanged(ControlState state) { }
		};
		drone.getNavDataManager().addStateListener(stateListener);
	}
	
	public void run()
	{
            while(!doStop) // control loop
            {
                try
                {
                    if ((tag != null) && (System.currentTimeMillis() - tag.getTimestamp() > 500)) // reset if too old (and not updated)
                        tag = null;

                    if(!IsFlying)
                    {
                        doStop = true;
                        break;
                    }

                    boolean isCentered = isTagCentered();

                    if (!isCentered) // tag visible, but not centered
                    {
                        if(tag != null)
                            centerTag();
                        else if(tag == null)
                            strayAround();
                    }                    
                    else if(Land)
                    {
                        landing();
                    }
                }
                catch(Exception exc)
                {
                    exc.printStackTrace();
                }
            }

            this.interrupt();
	}

	public void onTag(Result result, float orientation)
	{
            if (result == null) // ToDo: do not call if no tag is present
                return;

            System.out.println("TagAlignmentAutoController: Tag found");

            tag = result;
            tagOrientation = orientation;
	}
	
	
	private boolean isTagCentered()
	{
            if (tag == null)
                    return false;

            // a tag is centered if it is
            // 1. if "Point 1" (on the tag the upper left point) is near the center of the camera  
            // 2. orientation is between 350 and 10 degrees

            int imgCenterX = TagAlignment.IMAGE_WIDTH / 2;
            int imgCenterY = TagAlignment.IMAGE_HEIGHT / 2;

            ResultPoint[] points = tag.getResultPoints();
            boolean isCentered = ((points[1].getX() > (imgCenterX - TagAlignment.TOLERANCE)) &&
                    (points[1].getX() < (imgCenterX + TagAlignment.TOLERANCE)) &&
                    (points[1].getY() > (imgCenterY - TagAlignment.TOLERANCE)) &&
                    (points[1].getY() < (imgCenterY + TagAlignment.TOLERANCE)));

            boolean isOriented = ((tagOrientation < 10) || (tagOrientation > 350));

            System.out.println("TagAlignmentAutoController: Tag centered ? " + isCentered + " Tag oriented ? " + isOriented);

            return isCentered && isOriented;
	}
	
	private boolean hasTagBeenVisited()
	{
		synchronized(tag)
		{
			for (int i=0; i < tagVisitedList.size(); i++)
			{
				if (tag.getText().equals(tagVisitedList.get(i)))
					return true;
			}
		}
		
		return false;
	}
	        
	private void strayAround() throws InterruptedException
	{
		int direction = new Random().nextInt() % 4;
		switch(direction)
		{
			case 0 : drone.getCommandManager().forward(SPEED); System.out.println("PaperChaseAutoController: Stray Around: FORWARD"); break;
			case 1 : drone.getCommandManager().backward(SPEED); System.out.println("PaperChaseAutoController: Stray Around: BACKWARD");break;
			case 2 : drone.getCommandManager().goLeft(SPEED); System.out.println("PaperChaseAutoController: Stray Around: LEFT"); break;
			case 3 : drone.getCommandManager().goRight(SPEED); System.out.println("PaperChaseAutoController: Stray Around: RIGHT");break;
		}
		
		Thread.currentThread().sleep(SLEEP);
	}
	
	private void centerTag() throws InterruptedException
	{
		String tagText;
		ResultPoint[] points;
		
		synchronized(tag)
		{
			points = tag.getResultPoints();	
			tagText = tag.getText();
		}
		
		int imgCenterX = TagAlignment.IMAGE_WIDTH / 2;
		int imgCenterY = TagAlignment.IMAGE_HEIGHT / 2;
		
		float x = points[1].getX();
		float y = points[1].getY();
		
		if ((tagOrientation > 10) && (tagOrientation < 180))
		{
			System.out.println("PaperChaseAutoController: Spin left");
			drone.getCommandManager().spinLeft(SPEED * 2);
			Thread.currentThread().sleep(SLEEP);
		}
		else if ((tagOrientation < 350) && (tagOrientation > 180))
		{
			System.out.println("PaperChaseAutoController: Spin right");
			drone.getCommandManager().spinRight(SPEED * 2);
			Thread.currentThread().sleep(SLEEP);
		}
		else if (x < (imgCenterX - TagAlignment.TOLERANCE))
		{
			System.out.println("PaperChaseAutoController: Go left");
			drone.getCommandManager().goLeft(SPEED);
			Thread.currentThread().sleep(SLEEP);
		}
		else if (x > (imgCenterX + TagAlignment.TOLERANCE))
		{
			System.out.println("PaperChaseAutoController: Go right");
			drone.getCommandManager().goRight(SPEED);
			Thread.currentThread().sleep(SLEEP);
		}
		else if (y < (imgCenterY - TagAlignment.TOLERANCE))
		{
			System.out.println("PaperChaseAutoController: Go forward");
			drone.getCommandManager().forward(SPEED);
			Thread.currentThread().sleep(SLEEP);
		}
		else if (y > (imgCenterY + TagAlignment.TOLERANCE))
		{
			System.out.println("PaperChaseAutoController: Go backward");
			drone.getCommandManager().backward(SPEED);
			Thread.currentThread().sleep(SLEEP);
		}
		else
		{
			System.out.println("TagAlignmentAutoController: Tag centered");
			drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 5);
			
			tagVisitedList.add(tagText);
		}
	}
	
	public void landOnTag()
	{
		Land = true;
	}
	
	/*public void abortLanding() throws InterruptedException
	{
		Land = false;
		drone.getCommandManager().hover();
		Thread.currentThread().sleep(3000);
		doStop = true;
	}*/
	
	private void landing() throws InterruptedException
	{
            if(Altitude <= LandingAltitude)
            {
                System.out.println("TagAlignmentAutoController: Landing started...");
                drone.getCommandManager().setLedsAnimation(LEDAnimation.GREEN, 10, 5);
                drone.getCommandManager().landing();
                Thread.currentThread().sleep(5000);
                doStop = true;
            }
            else
            {
                System.out.println("TagAlignmentAutoController: Go Down");
                drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_ORANGE, 10, 5);
                drone.getCommandManager().down(30);
                drone.getCommandManager().setMaxAltitude(Altitude);
                Thread.currentThread().sleep(SLEEP);
            }
	}	
	
}
