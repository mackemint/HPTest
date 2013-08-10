package se.macke.hptest;

import android.util.Log;

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

	private static final String DEBUG_TAG = "Destination";

	private HPMainActivity _main;
	
	private int _cc;

	/**
	 * The value sent to the output queue
	 */
	private int _destinationValue;
	
	/**
	 * Creates a new destination
	 * @param cc - the CC message
	 * @param main main activity
	 */
	public DestinationProxy(int cc, HPMainActivity main)
	{
		_main = main;
		
		_cc = cc;

		_destinationValue = 100;

		Log.i(DEBUG_TAG,"Setting initial destination value to" + _destinationValue);

	}

	/**
	 * Used for setting the destination value
	 * @param value
	 */
	public void setValue(int value)
	{
		Log.i(DEBUG_TAG,"Setting destination value to queue");
		
		_destinationValue = value;
		_main.addCcToQueue(_cc, _destinationValue);
//		System.out.printf("Destination %d was set to: %d/n",_cc, _destinationValue);
	}


	public int getValue()
	{
		System.out.println("Returned destination value of: " + _destinationValue);
		return _destinationValue;
	}



}
