package TagAlignment;


import ImageAnalysis.ImageAnalyser;
import TagAlignment.TagAlignment;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

import de.yadrone.apps.controlcenter.plugins.keyboard.KeyboardCommandManager;
import de.yadrone.apps.paperchase.TagListener;
import de.yadrone.base.IARDrone;
import de.yadrone.base.video.ImageListener;
import java.awt.Color;
import java.awt.Point;

public class BildanalysGUI extends JFrame implements ImageListener, TagListener
{
	private Font font = new Font("SansSerif", Font.BOLD, 14);
	private ImageAnalyser imageAnalyser;
	private IARDrone drone;
	
	private BufferedImage image;
	private Result result;
	private String orientation;
	
	private JPanel contentPane;
	
	
	public BildanalysGUI(final IARDrone drone)
	{
		super("YADrone Paper Chase");
                this.imageAnalyser = new ImageAnalyser();
		this.drone = drone;
		
        setSize(TagAlignment.IMAGE_WIDTH, TagAlignment.IMAGE_HEIGHT);
        setVisible(true);
        
        addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				drone.stop();
				System.exit(0);
			}
		});
        
        contentPane = new JPanel() {
        	public void paint(Graphics g)
        	{
        		if (image != null)
        		{
        			g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        			g.setColor(Color.RED);
                                Point[] pointss = imageAnalyser.getLocation();
                                g.drawLine(pointss[0].x, pointss[0].y, pointss[1].x, pointss[1].y);
                                g.drawString("Angle: " + imageAnalyser.getAngle(), imageAnalyser.getOrigin().x, imageAnalyser.getOrigin().y);
                             //   g.setColor(Color.RED);
                             //   g.drawRect(test[0], test[1], test[2] - test[0], test[3] - test[1]);
                                
        			// draw tolerance field
        		//	g.setColor(Color.RED);
    				
    				int imgCenterX = TagAlignment.IMAGE_WIDTH / 2;
    				int imgCenterY = TagAlignment.IMAGE_HEIGHT / 2;
    				int tolerance = TagAlignment.TOLERANCE;
    				
    				/*g.drawPolygon(new int[] {imgCenterX-tolerance, imgCenterX+tolerance, imgCenterX+tolerance, imgCenterX-tolerance}, 
						      		  new int[] {imgCenterY-tolerance, imgCenterY-tolerance, imgCenterY+tolerance, imgCenterY+tolerance}, 4);
    				*/
    				
        			if (result != null)
        			{
        				ResultPoint[] points = result.getResultPoints();
        				ResultPoint a = points[1]; // top-left
        				ResultPoint b = points[2]; // top-right
        				ResultPoint c = points[0]; // bottom-left
        				ResultPoint d = points.length == 4 ? points[3] : points[0]; // alignment point (bottom-right)
        				
        				g.setColor(Color.GREEN);
        				
        				g.drawPolygon(new int[] {(int)a.getX(),(int)b.getX(),(int)d.getX(),(int)c.getX()}, 
  						      new int[] {(int)a.getY(),(int)b.getY(),(int)d.getY(),(int)c.getY()}, 4);
        				
        				g.setColor(Color.RED);
        				g.setFont(font);
        				g.drawString(result.getText(), (int)a.getX(), (int)a.getY());
        				g.drawString(orientation, (int)a.getX(), (int)a.getY() + 20);
        				
        				if ((System.currentTimeMillis() - result.getTimestamp()) > 1000)
        				{
        					result = null;
        				}
        			}
        		}
        		else
        		{
        			g.drawString("Waiting for Video ...", 10, 20);
        		}
        	}

        };
        setContentPane(contentPane);
	}
	
	private long imageCount = 0;
	
	public void imageUpdated(BufferedImage newImage)
    {
		if ((++imageCount % 2) == 0)
			return;
		
        	image = imageAnalyser.analyse(newImage);
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				contentPane.repaint();
			}
		});
    }
	
	public void onTag(Result result, float orientation)
	{
		if (result != null)
		{
			this.result = result;
			this.orientation = orientation + "s";
		}
//		else
//		{
//			this.result = null;
//			this.orientation = "n/a �";
//		}
	}
}
