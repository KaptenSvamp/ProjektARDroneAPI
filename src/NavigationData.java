
import java.awt.image.BufferedImage;

import de.yadrone.base.*;
import de.yadrone.base.navdata.*;
import de.yadrone.base.video.*;
import java.util.ArrayList;

public class NavigationData {
	private final IARDrone Drone;
        private final NavDataManager Manager;
    
	private boolean Initiated;
	public boolean isInitiated(){return Initiated;}
	
	// System start time (OS-time)[ms from 1970] and time since start [ms] 
	private long SystemStartTime;
	public long GetTimeSinceStart(){return System.currentTimeMillis() - SystemStartTime;}
	
	// Current time stamp from Drone.
	// CurrentTimeStamp [s]
	// CurrentTimeStampU [us]
	private long CurrentTimeStamp;
	public long GetCurrentTimeStamp(){return CurrentTimeStamp;}
	
	private long CurrentTimeStampU;
	public long GetCurrentTimeStampU(){return CurrentTimeStampU;}
	
	// Distance moved since last reset [m].
	// Calculated by velocity*LastVelocityReadTime.
	private long LastVelocityReadTime;
	
	private double MovedX;
	public double GetMovedX(){return MovedX;}
	public void ResetMovedX(){MovedX = 0;}
	
	private double MovedY;
	public double GetMovedY(){return MovedY;}
	public void ResetMovedY(){MovedY = 0;}
	
	private double MovedZ;
	public double GetMovedZ(){return MovedZ;}
	public void ResetMovedZ(){MovedZ = 0;}
	
	public double[] GetMovedXYZ(){return new double[]{MovedX, MovedY, MovedZ};}
	public void ResetMovedXYZ(){ResetMovedX();ResetMovedY();ResetMovedZ();}
	
	// Current pitch roll and yaw [degrees]
	private float Pitch;
	public float GetPitch(){return Pitch;}
	
	private float Roll;
	public float GetRoll(){return Roll;}
	
	private float Yaw;
	public float GetYaw(){return Yaw;}
	
	public float[] GetPitchRollYaw(){return new float[]{Pitch, Roll, Yaw};}
	
	// Current altitude [mm]
	private int Altitude;
	public int GetAltitude(){return Altitude;}
	
	// Current battery level [%]
	private int BatteryLevel;
	public int GetBatteryLevel(){return BatteryLevel;}
	
	// Current battery voltage (raw!).
	private int BatteryVoltage;
	public int GetBatteryVoltage(){return BatteryVoltage;}
	
	// Current velocity x, y and z [mm/s]
	private float VelocityX;
	public float GetVelocityX(){return VelocityX;}
	
	private float VelocityY;
	public float GetVelocityY(){return VelocityY;}
	
	private float VelocityZ;
	public float GetVelocityZ(){return VelocityZ;}
	
	public float[] GetVelocityXYZ(){return new float[]{VelocityX, VelocityY, VelocityZ};}
	
	// Current image from camera/cameras
	private BufferedImage CurrentImage;
	public BufferedImage GetCameraImage(){return CurrentImage;}
	
	// Current state.
	private boolean IsFlying;
	public boolean GetIsFlying(){return IsFlying;}
	
	// Constructor
	public NavigationData(IARDrone drone, long systemStartTime)
	{
            Drone = drone;
            Manager = drone.getNavDataManager();

            SystemStartTime = systemStartTime; 
            LastVelocityReadTime = System.currentTimeMillis();

            ResetMovedXYZ();

            Initiated = InitListeners();
	}
	
	private boolean InitListeners()
	{
            try
            {
                Manager.addTimeListener(new TimeListener()
                {
                    public void timeReceived(int seconds, int useconds)
                    {
                        CurrentTimeStamp = seconds;
                        CurrentTimeStampU = useconds;
                    }
                });

                Manager.addAttitudeListener(new AttitudeListener() {

                    private NavdataLogger logger;

                    public void attitudeUpdated(float pitch, float roll, float yaw)
                    {
                        //System.out.println("pitch: " + pitch + " roll: " + roll + " yaw:" + yaw);

                        Pitch = pitch;
                        Roll = roll;
                        Yaw = yaw;

                        if(logger == null)
                        {
                            logger = new NavdataLogger("Attitude", "pitch" + NavdataLogger.sep + "roll" + NavdataLogger.sep + "yaw");
                        }

                        String[] data = {pitch+"", roll+"", yaw+""};

                        logger.LogData(data, GetTimeSinceStart(), CurrentTimeStamp);
                    }

                    public void attitudeUpdated(float pitch, float roll) { }
                    public void windCompensation(float pitch, float roll) { }
                });

                Manager. addAltitudeListener(new AltitudeListener(){
                    private NavdataLogger logger;

                    @Override
                    public void receivedAltitude(int altitude) {

                        Altitude = altitude;

                        System.out.println("altitude: " + altitude);

                        if(logger == null)
                        {
                            logger = new NavdataLogger("Altitude", "altitude");
                        }

                        String[] data = {altitude+""};

                        logger.LogData(data, GetTimeSinceStart(), CurrentTimeStamp);
                    }

                    @Override
                    public void receivedExtendedAltitude(Altitude arg0) {

                    }

                });

                Manager.addUltrasoundListener(new UltrasoundListener()
                {
                    @Override
                    public void receivedRawData(UltrasoundData ud) {

                    }
                });

                Manager.addBatteryListener(new BatteryListener() {
                    
                    public void batteryLevelChanged(int percentage)
                    {
                        BatteryLevel = percentage;
                        //System.out.println("Battery: " + percentage + " %");
                    }

                    public void voltageChanged(int vbat_raw) {
                        BatteryVoltage = vbat_raw;
                    }
                });

                Manager.addVelocityListener(new VelocityListener()
                {
                    private NavdataLogger logger;
                    private NavdataLogger distanceLogger;

                    @Override
                    public void velocityChanged(float vx, float vy, float vz)
                    {
                        VelocityX = vx;
                        VelocityY = vy;
                        VelocityZ = vz;

                        //System.out.println("vx: " + vx + " vy" + vy + " vz" + vz);

                        if(logger == null)
                        {
                            logger = new NavdataLogger("Velocity", "vx" + NavdataLogger.sep + "vy" + NavdataLogger.sep + "vz");
                            distanceLogger = new NavdataLogger("DistanceTraveled", "MovedX" + NavdataLogger.sep +"MovedY" + NavdataLogger.sep + "MovedZ");
                        }

                        String[] data = {vx+"",vy+"",vz+""};

                        long timeSinceStart = GetTimeSinceStart();
                        long timeStamp = CurrentTimeStamp;

                        logger.LogData(data, timeSinceStart, timeStamp);

                        long temp = System.currentTimeMillis();

                        double time = (temp - LastVelocityReadTime) / 1000;

                        LastVelocityReadTime = temp;

                        MovedX += (vx)*time;
                        MovedY += (vy)*time;
                        MovedZ += (vz)*time;

                        String[] data2 = {MovedX+"",MovedY+"",MovedZ+""};

                        distanceLogger.LogData(data2, timeSinceStart, timeStamp);

                            //System.out.println("vx: " + (vx/1000)*time + " vy: " + (vy/1000)*time + " vz: " + (vz/1000)*time);

                    }
                });

                Manager.addAcceleroListener(new AcceleroListener()
                {
                    @Override
                    public void receivedPhysData(AcceleroPhysData arg0) {
                    }

                    @Override
                    public void receivedRawData(AcceleroRawData arg0) {
                            // TODO Auto-generated method stub

                    }

                });

                Drone.getVideoManager().addImageListener(new ImageListener() {

                    @Override
                    public void imageUpdated(BufferedImage newImage)
                    {
                        try {
                            CurrentImage = newImage;
                                /*
                            /*File outputfile = new File("C:\\temp\\cam"+ (System.currentTimeMillis() - SystemStartTime) + ".png");
                            ImageIO.write(newImage, "png", outputfile);*/
                        } catch (Exception e) {

                        }
                    }
                });

                Manager.addStateListener(new StateListener(){

                        @Override
                        public void controlStateChanged(ControlState state) {
                        }

                        @Override
                        public void stateChanged(DroneState state) {
                                IsFlying = state.isFlying();

                        }
                });
                
                
                Manager.addPWMlistener(new PWMlistener() {

                    private NavdataLogger logger; 
                    
                    @Override
                    public void received(PWMData pwmd) {
                                                
                        int[] current = pwmd.getCurrentMotor();
                        short[] motor = pwmd.getMotor();
                        short[] satMotor = pwmd.getSatMotor();
                        int[] URPY = pwmd.getUPRY();
                        int[] UGazPlanifPRY = pwmd.getUPlanifPRY();
                        float vzRef = pwmd.getVzRef();
                        float yawUI = pwmd.getYawUI();
                        float altitudeDer = pwmd.getAltitudeDer();
                        float altitudeIntegral = pwmd.getAltitudeIntegral();
                        float altitudeProp = pwmd.getAltitudeProp();
                        float gazAltitude = pwmd.getGazAltitude();
                        float gazFeedForward = pwmd.getGazFeedForward();
                        
                        ArrayList dataList = new ArrayList<String>();

                        String description = "";

                        for(int i=0; i<current.length; i++)
                        {
                            dataList.add(current[i] + "");
                            description += "current" + i + NavdataLogger.sep;
                        }

                        for(int i=0; i<motor.length; i++)
                        {
                            dataList.add(motor[i] + "");
                            description += "motor" + i + NavdataLogger.sep;
                        }

                        for(int i=0; i<satMotor.length; i++)
                        {
                            dataList.add(satMotor[i] + "");
                            description += "satMotor" + i + NavdataLogger.sep;
                        }

                        for(int i=0; i<URPY.length; i++)
                        {
                            dataList.add(URPY[i] + "");
                            description += "URPY" + i + NavdataLogger.sep;
                        }

                        for(int i=0; i<UGazPlanifPRY.length; i++)
                        {
                            dataList.add(UGazPlanifPRY[i] + "");
                            description += "UGazPlanifPRY" + i + NavdataLogger.sep;
                        }

                        description += "vzRef" + NavdataLogger.sep;
                        dataList.add(vzRef + "");

                        description += "yawUI" + NavdataLogger.sep;
                        dataList.add(yawUI + "");

                        description += "altitudeDer" + NavdataLogger.sep;
                        dataList.add(altitudeDer + "");

                        description += "altitudeIntegral" + NavdataLogger.sep;
                        dataList.add(altitudeIntegral + "");

                        description += "altitudeProp" + NavdataLogger.sep;
                        dataList.add(altitudeProp + "");

                        description += "gazAltitude" + NavdataLogger.sep;
                        dataList.add(gazAltitude + "");

                        description += "gazFeedForward";
                        dataList.add(gazFeedForward + "");

                        if(logger == null)
                        {
                            logger = new NavdataLogger("PWM Data", description);
                        }
                        
                        String[] dataArray = new String[dataList.size()];
                        dataArray = (String[])dataList.toArray(dataArray);
                        
                        long timeSinceStart = GetTimeSinceStart();
                        long timeStamp = CurrentTimeStamp;
                        
                        logger.LogData(dataArray, timeSinceStart, timeStamp);
                    }
                });
                
                Manager.addTrimsListener(new TrimsListener(){

                    private NavdataLogger logger;
                    
                    @Override
                    public void receivedTrimData(float angularRates, float eulerAnglesTheta, float eulerAnglesPhi) {
                        if(logger == null)
                        {
                            logger = new NavdataLogger("Trim Data", "AngularRates" 
                                    + NavdataLogger.sep + "EulerAnglesTheta"
                                    + NavdataLogger.sep + "EulerAnglesPhi");
                        }
                        
                        String[] data = {angularRates+"", eulerAnglesTheta+"", eulerAnglesPhi+""};
                        
                        long timeSinceStart = GetTimeSinceStart();
                        long timeStamp = CurrentTimeStamp;
                        
                        logger.LogData(data, timeSinceStart, timeStamp);
                        
                    }
                });

            }
            catch(Exception e)
            {
                System.out.println("------ ERROR -----");
                System.out.println("NavData: Failed to initiate listeners.");
                System.out.println("Exception: " + e.toString());
                System.out.println("------ END OF ERROR -----");

                return false;
            }

            return true;
	}
	
	
	
}
