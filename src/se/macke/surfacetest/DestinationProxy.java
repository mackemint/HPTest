package se.macke.surfacetest;

import android.util.Log;
import android.widget.SeekBar;

/**
 * This class is used for adding messages to the output queue.
 * Proxy class for getting I/O to and from the DAW.
 * 
 * In a later stage this will be used to compare fader values in the DAW with the saved values on the controller.
 * This will make everything behave in a Latest Takes Presidence way.
 * @author macke
 *
 */
public class DestinationProxy 
{

	/**
	 * Concatenating debug message
	 */
	private String DEBUG_TAG = STMainActivity.PROJECT_TAG + ":Destination";

	private STMainActivity _main;
	
	private int _cc;

	/**
	 * The value sent to the output queue
	 */
	private int _destinationValue;

	/**
	 * The CC# transmitted
	 */
	private int _ccNumber;

	/**
	 * The value in 7 bit sent to the output queue
	 */
	private int _ccValue;
	

		

	/**
	 * Creates a new destination
	 * 
	 * @param cc - the CC message
	 * @param main main activity
	 */
	public DestinationProxy(int cc, STMainActivity main) 
	{

		_main = main;
		
		_destinationValue = 100;
			
		_ccNumber = cc;

		_ccValue = 100;

		Log.i(DEBUG_TAG,"Setting initial destination value to" + _ccValue);
	}

	/**
	 * Used for setting the destination value
	 * @param value
	 */
	public void setValue(int value)
	{
		Log.i(DEBUG_TAG,"Setting destination value to queue");
		
		_ccValue = value;
		_main.addCcToQueue(_ccNumber, _ccValue);
//		System.out.printf("Destination %d was set to: %d/n",_cc, _destinationValue);
	}


	public int getValue()
	{
		System.out.println("Returned destination value of: " + _ccValue);
		return _ccValue;
	}



}
