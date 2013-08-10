package se.macke.hptest;

import android.util.Log;




/**
 * An input handler has a real value that is read by the IOIO.
 * It has an analog input pin and a destination.
 * Since one analog input can edit many destinations, it uses a Greatest Takes Presidence
 * method to compare its real value to the destinations'.
 * This decides if it shall affect the destination value or not.
 * 
 * @author macke
 */
public class InputHandler
{

	/**
	 * The value received from the IOIO AnalogInput
	 */
	private int _inputVal;

	/**
	 * The destination to be changed
	 * 
	 */
	private DestinationProxy _destination;

	/**
	 * The sources' value.
	 * Used for determining if the value is close enough for takeover
	 */
	private int _lastVal;

	/**
	 * Used when the value is unchanged since the last time.
	 */
	private boolean _lastNotSame;

	/**
	 * Decides if the destination shall be affected by manipulation or not.
	 */
	private boolean _takeOver;

	private final static String DEBUG_TAG = "InputHandler";

      
	/**
	 * Used when the source talks directly to the InputHandler
	 * 
	 * @param destination
	 */
	public InputHandler(DestinationProxy destination) 
	{
		_destination = destination;

	}

	/**
	 * Used when focus of the destination is changed.
	 * 
	 * @param destination - the focus of the handler.
	 */
	public void setDestination(DestinationProxy destination)
	{
		_destination = destination;
	}

	/**
	 * Used to control separation between GUI and hardware input.
	 * 
	 * @param active - if the destination shall be controlled by the hardware or not
	 */
	public void setTakeOver(Boolean active)
	{
		_takeOver = active;
	}

	/**
	 * Used when scrolling sideways in the layout
	 * TBC
	 */
	public void changeFocus()
	{
		_lastVal = _destination.getValue();	
	}

	/**
	 * Used for setting the destination value to the value of the input source.
	 * This happens whenever the fader is in takeover mode.
	 * 
	 * To avoid flooding, the destination only gets a new value if it isn't the same as the old one.
	 * 

	 */
	public void setValue(int input)
	{
		Log.i(DEBUG_TAG,"setValue Method");


		_inputVal = input;
		/**
		 * Used for Greatest Takes Presidence
		 */
		float destinationVal = _destination.getValue();

		//If the fader is in takeover mode, the destination gets the value of the input source
		//This only happens if the input source has changed since the last time
		if (_lastVal == _inputVal)
			_lastNotSame = false;
		else 
			_lastNotSame = true;

		Log.i(DEBUG_TAG,"lastVal is: " + _lastVal + " _inputVal is: " + _inputVal);


		if(destinationVal == _inputVal)	
			_takeOver = true;


		// Sets the destination to True to mark that takeover is happening
		if(_lastNotSame && _takeOver)
		{
			_lastVal = _inputVal;
			setDestinationValue(_lastVal);
		}


	}


	/**
	 * Filters the value before setting it to the Destination
	 */
	public void setDestinationValue(int input)
	{
		Log.i(DEBUG_TAG,"Setting destination value to" + input);

		_destination.setValue(input);

	}

	public DestinationProxy getDestination() 
	{
		return _destination;
	}

	/**
	 * Used for setting an initial value to the filter.
	 * This is to guarantee that the initial value is where the fader is.
	 * 
	 * @param previous
	 */
	public void setInitial(int previous) 
	{
		Log.i(DEBUG_TAG,"Setting initial value to" + previous);

		
		_lastVal = previous;	
	}
}
