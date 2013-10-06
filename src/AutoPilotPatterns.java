
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
    
    private Pattern CurrentPattern;
    public Pattern GetCurrentPattern(){return CurrentPattern;}
    public void SetCurrentPattern(Pattern pattern){CurrentPattern = pattern;}
    
    public AutoPilotPatterns(IARDrone drone)
    {
        Drone = drone;
        Command = drone.getCommandManager();
    }
    
    @Override
    public void doWork()
    {
        if(CurrentPattern == null)
            return;
        
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
            default:
            {
                return;
            }
            
        }
    }
    
    private void TagAlignment()
    {
        TagAlignment tagAlignment = new TagAlignment(Drone);
        
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


