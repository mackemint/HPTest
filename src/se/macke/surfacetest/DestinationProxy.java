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

	private String DEBUG_TAG = STMainActivity.PROJECT_TAG + ":Destination";

	private STMainActivity _main;
	
	private SeekBar _seekBar;

	/**
	 * The value sent to the output queue
	 */
	private int _destinationValue;
	
	/**
	 * Creates a new destination
	 * @param row - the CC message
	 * @param main main activity
	 * @param i 
	 */
	public DestinationProxy(SeekBar seekBar, STMainActivity main)
	{
		
		_main = main;
		_destinationValue = 100;
		
		_seekBar = seekBar;

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
		_main.addCcToQueue(_seekBar, _destinationValue);
//		System.out.printf("Destination %d was set to: %d/n",_cc, _destinationValue);
	}


	
	public int getValue()
	{
		_destinationValue = _seekBar.getProgress();
		
		Log.i(DEBUG_TAG, "Got destination value of: " + _destinationValue);
		return _destinationValue;
	}



}
