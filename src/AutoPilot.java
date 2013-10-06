import NotificationThread.TaskListener;
import TagAlignment.TagAlignment;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.*;

/*
 * @author Rasmus Bjerstedt
 */
public class AutoPilot {
	private final IARDrone Drone;
	private final CommandManager Command;
	
	private boolean autoPilotEngaged;
	public boolean IsAutoPilotEngaged(){ return autoPilotEngaged;}
	
	//private TagAlignment TagAlignment;
        private AutoPilotPatterns AutoPilotPatterns;
        private Thread AutoPilotPatternsThread;
	
               
	public AutoPilot(IARDrone drone)
	{
            Drone = drone;
            Command = Drone.getCommandManager();
            
            //TagAlignment = new TagAlignment(drone, true);
            
            AutoPilotPatterns = new AutoPilotPatterns(drone);
            
            AutoPilotPatterns.addListener(new TaskListener(){
                @Override
                public void threadComplete(Runnable runner) {
                    IndicatePatternStopped();
                }
                
            });

        }
        
        public void StopAutoPilot()
        {
            try
            {
                if(AutoPilotPatternsThread != null && autoPilotEngaged)
                {
                    AutoPilotPatternsThread.stop();
                }
            
                //if(TagAlignment.IsEnabled())
                //    TagAlignment.enableAutoControl(false);
            }
            catch(Exception e)
            {
                
            }
            
            Command.landing();
            
            IndicatePatternStopped();
        }
	
	private void IndicatePatternStarted()
	{
            autoPilotEngaged = true;
            Command.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 3, 5);
	}
	
	private void IndicatePatternStopped()
	{
            System.out.println("*** PATTERN DONE ***");
            
            AutoPilotPatternsThread = null;
            
            autoPilotEngaged = false;
            Command.setLedsAnimation(LEDAnimation.BLINK_GREEN, 3, 5);
	}
	
	public void RunTagAlignment()
	{
            IndicatePatternStarted();
            //TagAlignment.enableAutoControl(true);
	}
	              
	public void RunTagAlignmentLanding()
	{
            IndicatePatternStarted();
            //TagAlignment.landOnTag();
	}
        
        public void RunTestThread()
        {
            IndicatePatternStarted();
            
            AutoPilotPatterns.SetCurrentPattern(Pattern.TestThread);
            
            AutoPilotPatternsThread = new Thread(AutoPilotPatterns);
            AutoPilotPatternsThread.start();
        }
	
        public void RunHoverAndLand(int ms)
        {
            if(autoPilotEngaged)
                StopAutoPilot();
            
            AutoPilotPatterns.SetCurrentPattern(Pattern.HoverAndLand);
            
            AutoPilotPatternsThread = new Thread(AutoPilotPatterns);
            AutoPilotPatternsThread.start();
            
            IndicatePatternStarted();
        }
	
        public void RunRLBF()
        {
            if(autoPilotEngaged)
                StopAutoPilot();
            
            AutoPilotPatterns.SetCurrentPattern(Pattern.RLBF);
            
            AutoPilotPatternsThread = new Thread(AutoPilotPatterns);
            AutoPilotPatternsThread.start();
            
            IndicatePatternStarted();
            
        }
        
	
}
