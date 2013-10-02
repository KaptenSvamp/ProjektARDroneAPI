import java.io.*;
import java.util.Date;


public class NavdataLogger {
		public String Path;
		public String FullPath;
		public String Name;
		public String Description;
		public String CurrentDateTime;
		
		private int SampleCount;
		public static final String ln = "\r\n";
		public static final String sep = "\t";
		
		public NavdataLogger(String name, String description)
		{
			Name = name;
			Description = description;
			Path = "C:\\temp\\data";
			CurrentDateTime = "TEST3";//new Date().toString().replace(":", "_");
			FullPath = Path + "\\" + name + " " + CurrentDateTime + ".txt";
			SampleCount = 0;
		}
				
		public void LogData(String[] data, long systemStamp, long timeStamp)
		{
                    SampleCount++;

                    boolean exists = new File(FullPath).isFile();
                    FileWriter out = null;

                    try
                    {
                        out = new FileWriter(FullPath, exists);

                        if(!exists)
                        {
                                out.write("[" + Name + ", " + CurrentDateTime + "]" + ln);
                                out.write("[Sample" + sep + "SystemStamp" + sep + "TimeStamp" + sep+ Description + "]" + ln);
                        }

                        String toPrint = SampleCount + sep + systemStamp + sep + timeStamp;

                        for(String d : data){
                                toPrint += sep + d;
                        }

                        out.write(toPrint + ln);

                        out.close();
                    }
                    catch(Exception e)
                    {				
                            System.out.println("NavdataLogger error: " + e);
                    }
                    finally
                    {
                            if(out != null)
                            {
                                    try
                                    {
                                            out.close();
                                    }
                                    catch(Exception ex)
                                    {}
                            }
                    }
		}
		/*
		public void LogData(String data)
		{
			boolean exists = new File(FullPath).isFile();
			FileWriter out = null;
			
			try
			{
				 out = new FileWriter(FullPath, exists);
				 
				 if(!exists)
				 {
					 out.write("[" + Name + "," + CurrentDateTime + "]" + "\r\n");
					 out.write("[System time milis\t" + Description + "]\r\n");
				 }
				 
				 String toPrint = System.currentTimeMillis() + "\t" + data; 
				 
				 out.write(toPrint + "\r\n");
				 
				 out.close();
			}
			catch(Exception e)
			{				
				System.out.println("NavdataLogger error: " + e);
			}
			finally
			{
				if(out != null)
				{
					try
					{
						out.close();
					}
					catch(Exception ex)
					{}
				}
			}
		}*/
}
