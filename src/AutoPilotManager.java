import NotificationThread.TaskListener;
import TagAlignment.BildanalysGUI;
import de.yadrone.apps.controlcenter.plugins.keyboard.KeyboardCommandManager;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.*;
import de.yadrone.base.navdata.AttitudeListener;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

/*
 * Manages the AutoPilot class. Starts, stops and sets current AutoPilot.FlyingPattern.
 * 
 * @author Rasmus Bjerstedt
 */
public class AutoPilotManager {
	private final IARDrone Drone;
	private final CommandManager Command;
	
	private boolean autoPilotEngaged;
	public boolean IsAutoPilotEngaged(){ return autoPilotEngaged;}
	
	//private TagAlignment TagAlignment;
        private AutoPilot AutoPilot;
        private Thread AutoPilotThread;
        BildanalysGUI gui;
        
        private float CurrentYaw;
               
	public AutoPilotManager(IARDrone drone)
	{
            Drone = drone;
            Command = Drone.getCommandManager();
            
            Command.setVideoChannel(VideoChannel.VERT);
            //Command.setVideoBitrate(25);
            //Command.setVideoCodec(VideoCodec.H264_720P_SLRS);
            
            AutoPilot = new AutoPilot(drone);
            
            AutoPilot.addListener(new TaskListener(){
                @Override
                public void threadComplete(Runnable runner) {
                    IndicatePatternStopped();
                }
                
            });
            
            /* Listener for setting ReferenceYaw */
            Drone.getNavDataManager().addAttitudeListener(new AttitudeListener() {

                    @Override
                    public synchronized void attitudeUpdated(float pitch, float roll, float yaw)
                    {
                        CurrentYaw = yaw;
                    }

                    @Override
                    public synchronized void attitudeUpdated(float pitch, float roll) { }
                    @Override
                    public synchronized void windCompensation(float pitch, float roll) { }
                });

        }
        
        public void SetReferenceYaw()
        {
            AutoPilot.SetReferenceYaw(CurrentYaw);
        }
        
        public void SetReferenceYaw(float yaw)
        {
            AutoPilot.SetReferenceYaw(yaw);
        }
        
        public void StopAutoPilot()
        {
            try
            {
                if(AutoPilotThread != null)
                {
                    AutoPilotThread.interrupt();
                    //AutoPilotPatternsThread.stop();
                }            
            }
            catch(Exception e)
            {
                AutoPilotThread.stop();
            }
            
            Command.hover();
            //Command.landing();
            
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
            
            if(gui != null)
            {
                Drone.getVideoManager().removeImageListener(gui);
                gui.getContentPane().removeAll();
                gui.dispose();
                gui = null;
            }
            
            AutoPilotThread = null;
            
            autoPilotEngaged = false;
            Command.setLedsAnimation(LEDAnimation.BLINK_GREEN, 3, 5);
	}
	
	public void RunTagAlignment()
	{
            IndicatePatternStarted();
            
            gui = new BildanalysGUI(Drone);
            Drone.getVideoManager().addImageListener(gui);
            
            AutoPilot.SetCurrentPattern(FlyingPattern.TagAlignment);
            
            AutoPilotThread = new Thread(AutoPilot);
            AutoPilotThread.start();
	}
	              
	public void RunTagAlignmentLanding()
	{
            if(AutoPilotThread == null)
                RunTagAlignment();
                
            AutoPilot.SetTagAlignmentLanding(true);
	}
        
        public void RunTestThread()
        {
            IndicatePatternStarted();
            
            AutoPilot.SetCurrentPattern(FlyingPattern.TestThread);
            
            AutoPilotThread = new Thread(AutoPilot);
            AutoPilotThread.start();
        }
        
        public void SetTestInTestThread()
        {
            AutoPilot.test = true;
        }
	
        public void RunHoverAndLand(int ms)
        {
            if(autoPilotEngaged)
                StopAutoPilot();
            
            AutoPilot.SetCurrentPattern(FlyingPattern.HoverAndLand);
            
            AutoPilotThread = new Thread(AutoPilot);
            AutoPilotThread.start();
            
            IndicatePatternStarted();
        }
	
        public void RunRLBF()
        {
            if(autoPilotEngaged)
                StopAutoPilot();
            
            AutoPilot.SetCurrentPattern(FlyingPattern.RLBF);
            
            AutoPilotThread = new Thread(AutoPilot);
            AutoPilotThread.start();
            
            IndicatePatternStarted();
            
        }
        
	
}
