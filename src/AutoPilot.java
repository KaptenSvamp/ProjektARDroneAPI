import NotificationThread.TaskListener;
import TagAlignment.BildanalysGUI;
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
            
            Drone.getCommandManager().setVideoChannel(VideoChannel.VERT);
            
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
                if(AutoPilotPatternsThread != null)
                {
                    AutoPilotPatternsThread.interrupt();
                    //AutoPilotPatternsThread.stop();
                }            
            }
            catch(Exception e)
            {
                AutoPilotPatternsThread.stop();
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
            
            BildanalysGUI gui = new BildanalysGUI(Drone);
            Drone.getVideoManager().addImageListener(gui);
            
            AutoPilotPatterns.SetCurrentPattern(Pattern.TagAlignment);
            AutoPilotPatterns.SetTagAlignmentLanding(false);
            
            AutoPilotPatternsThread = new Thread(AutoPilotPatterns);
            AutoPilotPatternsThread.start();
	}
	              
	public void RunTagAlignmentLanding()
	{
            if(AutoPilotPatternsThread == null)
                RunTagAlignment();
                
            AutoPilotPatterns.SetTagAlignmentLanding(true);
	}
        
        public void RunTestThread()
        {
            IndicatePatternStarted();
            
            AutoPilotPatterns.SetCurrentPattern(Pattern.TestThread);
            
            AutoPilotPatternsThread = new Thread(AutoPilotPatterns);
            AutoPilotPatternsThread.start();
        }
        
        public void SetTestInTestThread()
        {
            AutoPilotPatterns.test = true;
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
