package TagAlignment;

import com.google.zxing.Result;
import de.yadrone.base.IARDrone;
import de.yadrone.apps.paperchase.TagListener;

public abstract class TagAlignmentAbstractController extends Thread implements TagListener
{
	protected boolean doStop = false;
	protected boolean Land = false;

	protected IARDrone drone;
	
	public TagAlignmentAbstractController(IARDrone drone)
	{
		this.drone = drone;
	}

	public abstract void run();
	
	public void onTag(Result result, float orientation)
	{

	}
	
	public void stopController()
	{
		doStop = true;
	}
	
	public void landOnTag()
	{
		Land = true;
	}
	
	public void abortLanding()
	{
		Land = false;
	}
}
