
import NotificationThread.NotificationThread;
import TagAlignment.TagAlignment;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;

/**
 *
 * @author Rasmus Bjerstedt
 */
public class AutoPilotPatterns extends NotificationThread{
    
    private IARDrone Drone;
    private CommandManager Command;
    
    private FlyingPattern CurrentPattern;
    public FlyingPattern GetCurrentPattern(){return CurrentPattern;}
    public void SetCurrentPattern(FlyingPattern pattern){CurrentPattern = pattern;}
    
    private TagAlignment TagAlignment;
    
    public boolean test = false;
    
    public AutoPilotPatterns(IARDrone drone)
    {
        Drone = drone;
        Command = drone.getCommandManager();
        TagAlignment = new TagAlignment(Drone);
        
        Drone.getVideoManager().addImageListener(TagAlignment);
    }
    
    public synchronized void SetReferenceYaw(float yaw){ TagAlignment.SetReferenceYaw(yaw);}
    public void SetReferenceYaw(){ TagAlignment.SetReferenceYaw();}
    public float GetReferenceYaw(){return TagAlignment.GetReferenceYaw();}
    
    @Override
    public void doWork()
    {
        if(CurrentPattern == null)
            return;
        
        try
        {
            switch(CurrentPattern)
            {
                case TestThread:
                {
                    TestThread();
                    break;
                }
                case HoverAndLand:
                {
                    HoverAndLand(5000);
                    break;
                }
                case RLBF:
                {
                    RLBF();
                    break;
                }
                case TagAlignment:
                {
                    TagAlignment();
                    break;
                }
                case TagAlignmentLanding:
                {
                    SetTagAlignmentLanding(true);
                    TagAlignment();
                    break;
                }
                case SpinLeftHover:
                {
                    SpinLeftHover();
                    break;
                }
                case SpinRightHover:
                {
                    SpinRightHover();
                }
                default:
                {
                    return;
                }

            }
        }
        catch(Exception e)
        {
            System.out.println("Pattern stopped: " + e);
        }
        
    }
    
    private void SpinLeftHover() throws InterruptedException
    {
        Command.spinLeft(30);
        Thread.sleep(1000);
        
        Command.spinLeft(10);
        Thread.sleep(500);
        
        Command.hover();
    }
    
    private void SpinRightHover() throws InterruptedException
    {
        Command.spinRight(30);
        Thread.sleep(1000);
        
        Command.hover();
    }
    
    private void TagAlignment()
    {
        TagAlignment.ControlLoop();
    }
    
    public void SetTagAlignmentLanding(boolean land)
    {
        TagAlignment.SetLanding(land);
    }
    
    private void TestThread()
    {
        try
        {
            int i = 0;
            while(true)
            {
                i++;
                System.out.println("Iteration: " + i);
                Thread.sleep(1000);
                
                if(test)
                    System.out.println("TEST!!!!!!!!!!!!!!!!!!!");
                
                if(i > 20)
                    break;
            }
        }
        catch(Exception e)
        {
            System.out.println("TEST THREAD: " + e);
        }
    }
    
    private void HoverAndLand(int ms)
	{
            try {
                Command.takeOff();
                Thread.sleep(5000);

                Command.hover();
                Thread.sleep(ms);

                Command.landing();
            }
            catch (InterruptedException e) {
                
                System.out.println("HOVER AND LAND: " + e);
                //e.printStackTrace();
            }
	}
    
    private void RLBF()
	{
            try
            {
                int speed = 30;

                Command.takeOff();
                Thread.sleep(5000);

                Command.hover();
                Thread.sleep(2000);

                Command.goLeft(speed);
                Thread.sleep(1000);

                Command.hover();
                Thread.sleep(2000);

                Command.goRight(speed);
                Thread.sleep(1000);

                Command.hover();
                Thread.sleep(2000);

                Command.forward(speed);
                Thread.sleep(1000);

                Command.hover();
                Thread.sleep(2000);

                Command.backward(speed);
                Thread.sleep(1000);

                Command.hover();
                Thread.sleep(2000);

                Command.landing();
            }
            catch(Exception e)
            {
                System.out.println("Pattern stopped: " + e);
            }

            
            /*
            System.out.println();
            System.out.println("******--- Pattern DONE ---******");
            System.out.println("Distances from original position:");
            System.out.println("MovedX: " + MovedX + " MovedY: " + MovedY + " MovedZ: " + MovedZ);
            */
	}
        
        private void FWBW(int speed, int duration)
        {
            try
            {
                Command.takeOff();
                Thread.sleep(5000);

                Command.hover();
                Thread.sleep(2000);
            
                Command.forward(speed);
                Thread.sleep(duration);
                
                Command.backward(speed);
                Thread.sleep(duration);
                
                Command.hover();
                Thread.sleep(2000);

                Command.landing();
            }
            catch(Exception e)
            {
            
            }
            
        }
    
}


