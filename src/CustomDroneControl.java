import de.yadrone.base.*;
import de.yadrone.base.command.*;

/*
 * This class handles controlling of the drone from outside sources (Validation tool) 
 * and creates and initiates AutoPilot and NavigationData.
 * 
 * This is basically a wrap-around class for the YADrone API with extras. 
 * 
 * @author Rasmus Bjerstedt
 */
public class CustomDroneControl {
    private final IARDrone Drone;
    private final CommandManager Command;

    public NavigationData NavigationData;
    public AutoPilot AutoPilot;

    private boolean Initiated;
    public boolean isInitiated(){return Initiated;}

    private long SystemStartTime;
    public long GetSystemStartTime(){return SystemStartTime;}
    public long GetSystemUpTime(){return SystemStartTime-System.currentTimeMillis();}

    public CustomDroneControl()
    {
        Drone = new ARDrone();
        Command = Drone.getCommandManager();
        Command.setOutdoor(false, true);
        SystemStartTime = 0;

        if(Start())
        {
            NavigationData = new NavigationData(Drone, SystemStartTime);
            Initiated = NavigationData.isInitiated();
            AutoPilot = new AutoPilot(Drone);

            FlatTrim();
        }

        //Command.setVideoChannel(VideoChannel.HORI);
        //Command.setVideoCodec(VideoCodec.H264_720P);
        //Command.setVideoCodecFps(1);
    }

    public boolean Start()
    {
        try
        {
            Drone.start();
            SystemStartTime = System.currentTimeMillis();
        }
        catch(Exception e)
        {
            Abort(e);

            return false;
        }

        return true;
    }

    public void TakeOff()
    {
        Command.takeOff();
    }

    public void Hover()
    {
        Command.hover();
    }

    public void GoRight(int speed)
    {
        Command.goRight(speed);
    }

    public void GoLeft(int speed)
    {
        Command.goLeft(speed);
    }

    public void GoForward(int speed)
    {
        Command.forward(speed);
    }

    public void GoBackward(int speed)
    {
        Command.backward(speed);
    }

    public void GoDown(int speed)
    {
        Command.down(speed);
    }

    public void SpinLeft(int speed)
    {
        Command.spinLeft(speed);
    }

    public void SpinRight(int speed)
    {
        Command.spinRight(speed);
    }

    public void Move(int speedX, int speedY, int speedZ, int speedSpin)
    {
        Command.move(speedX, speedY, speedZ, speedSpin);
    }

    public void Land()
    {
        Command.landing();
    }

    public void FlatTrim()
    {
        Command.flatTrim();
    }

    public void Abort(Exception e)
    {
        if(e != null)
        {
            System.out.println("---- ABORT WITH EXCEPTION ----");
            e.printStackTrace();
        }
        else
        {
            System.out.println("---- SYSTEM ABORT ----");
        }

        if(Drone != null)
            Drone.stop();
        
        if(AutoPilot != null && AutoPilot.IsAutoPilotEngaged())
        {
            AutoPilot.StopAutoPilot();
        }
    }

    public void Abort()
    {
        Abort(null);
    }
		
		
		
		
}
