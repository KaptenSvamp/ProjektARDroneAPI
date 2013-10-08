
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import de.yadrone.base.*;
import de.yadrone.base.navdata.*;

public class main
{
	public static CustomDroneControl droneControl = null;
	
	public static void main (String agrs[])
	{
            
            try
            {
                droneControl = new CustomDroneControl();
            }
            catch(Exception e)
            {
                if(droneControl != null)
                    droneControl.Abort(e);
                else
                    e.printStackTrace();

                System.exit(-1);
            }
            
            if(droneControl.isInitiated())
            {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        CreateJFrame();
                    }
                });
                
            }
	}
	
	public static void CreateJFrame()
	{
            JFrame f = new JFrame("Drone control");
	    f.setSize(450, 200);
            
	    Container content = f.getContentPane();
	    content.setBackground(Color.white);
	    content.setLayout(new FlowLayout());
	    
            JButton testButton = new JButton("SetTestBool");
	    testButton.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                    droneControl.AutoPilot.SetTestInTestThread();
                }
	    });
            
            JButton iterationButton = new JButton("Iteration");
	    iterationButton.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                    droneControl.AutoPilot.RunTestThread();
                }
	    });
            
	    JButton hoverButton = new JButton("Hover");
	    hoverButton.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try
                    {
                        droneControl.TakeOff();
                        
                        Thread.sleep(5000);
                        
                        droneControl.Hover();
                    }
                    catch(Exception ee)
                    {}
                    
                    //droneControl.AutoPilot.RunHoverAndLand(4000);
                }
	    });
	    
	    JButton landButton = new JButton("Land");
	    landButton.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                        droneControl.Land();
                }
	    });
	    
	    JButton tagAlignment = new JButton("ENABLE Tag Alignment");
	    tagAlignment.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                        droneControl.AutoPilot.RunTagAlignment();
                }
	    });
	    
	    JButton disableTagAlignment = new JButton("DISABLE AutoPilot");
	    disableTagAlignment.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                        droneControl.AutoPilot.StopAutoPilot();
                }
	    });
	    
	    JButton tagAlignmentLanding = new JButton("Tag Alignment Landing");
	    tagAlignmentLanding.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                        droneControl.AutoPilot.RunTagAlignmentLanding();
                }
	    });
	    
            JButton setReferenceYaw = new JButton("Set reference yaw");
	    setReferenceYaw.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                    droneControl.AutoPilot.SetReferenceYaw();
                }
	    });
            
            JButton spintLeft = new JButton("Spin left");
	    spintLeft.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                    droneControl.SpinLeft(30);
                }
	    });
            
            JButton spintRight = new JButton("Spin right");
	    spintRight.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                    droneControl.SpinRight(30);
                }
	    });
	    
            content.add(hoverButton);
            content.add(landButton);
            content.add(spintLeft);
            content.add(spintRight);
	    
	    content.add(setReferenceYaw);
            content.add(tagAlignment);
	    content.add(disableTagAlignment);
	    content.add(tagAlignmentLanding);
	    
            content.add(iterationButton);
            //content.add(testButton);
	    
	    
	    f.setVisible(true);
		
	}

}

