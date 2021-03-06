package Main;


import de.yadrone.apps.controlcenter.plugins.keyboard.KeyboardCommandManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.awt.event.KeyEvent;

public class main
{
	public static CustomDroneControl droneControl = null;
	private static KeyboardCommandManager keyboardCommandManager;
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
            
            keyboardCommandManager = new KeyboardCommandManager(droneControl.Drone);
		
            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.addKeyEventDispatcher( new KeyEventDispatcher() {
    	
                public boolean dispatchKeyEvent(KeyEvent e)
		{
                    if (e.getID() == KeyEvent.KEY_PRESSED) 
                    {
                        keyboardCommandManager.keyPressed(e);
                    } 
                    else if (e.getID() == KeyEvent.KEY_RELEASED) 
                    {
                        keyboardCommandManager.keyReleased(e);
                    }
                    return false;
		}
            });
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
                    droneControl.AutoPilotManager.SetTestInTestThread();
                }
	    });
            
            JButton iterationButton = new JButton("Iteration");
	    iterationButton.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                    droneControl.AutoPilotManager.RunTestThread();
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
                        
                        droneControl.AutoPilotManager.SetReferenceYaw();
                    }
                    catch(Exception ee)
                    {}
                    
                    //droneControl.AutoPilotManager.RunHoverAndLand(4000);
                }
	    });
	    
	    JButton landButton = new JButton("Land");
	    landButton.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                        droneControl.AutoPilotManager.StopAutoPilot();
                        droneControl.Land();
                }
	    });
	    
	    JButton tagAlignment = new JButton("ENABLE Tag Alignment");
	    tagAlignment.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                        droneControl.AutoPilotManager.RunTagAlignment();
                }
	    });
	    
	    JButton disableTagAlignment = new JButton("DISABLE AutoPilot");
	    disableTagAlignment.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                        droneControl.AutoPilotManager.StopAutoPilot();
                }
	    });
	    
	    JButton tagAlignmentLanding = new JButton("Tag Alignment Landing");
	    tagAlignmentLanding.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                        droneControl.AutoPilotManager.RunTagAlignmentLanding();
                }
	    });
	    
            JButton setReferenceYaw = new JButton("Set reference yaw");
	    setReferenceYaw.addActionListener(new ActionListener()
	    {
                @Override
                public void actionPerformed(ActionEvent e) {
                    droneControl.AutoPilotManager.SetReferenceYaw();
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

