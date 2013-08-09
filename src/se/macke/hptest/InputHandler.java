package se.macke.hptest;


import android.widget.SeekBar;


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
	 * The source of the input
	 * TODO Might be changed to something that actually can provide a value, like an Analog Input
	 */
	private SeekBar _inputSource;
	
	/**
	 * The value received from the IOIO AnalogInput
	 */
	private int _inputVal;

	/**
	 * The destination to be changed
	 * 
	 */
	private Destination _destination;
	
	/**
	 * The sources' value.
	 * Used for determining if the value is close enough for takeover
	 */
	private float _lastVal;

	/**
	 * Used when the value is unchanged since the last time.
	 */
	private boolean _lastSimilar;

	/**
	 * Decides if the destination shall be affected by manipulation or not.
	 */
	private boolean _takeOver;

	private LowPassFilter _lpf;
	
	
	/**
	 * Creates a new InputHandler that takes source and destination as parameters
	 * 
	 * @param source - the source seekBar
	 * @param destination - the destination SeekBar
	 */
	public InputHandler(SeekBar source, Destination destination) 
	{
		_inputSource = source;
		_destination = destination;
	}

	/**
	 * Used if one wishes to change the input source
	 * @param input
	 */
	public void setInputSource(SeekBar input)
	{
		_inputSource = input;
	}
	/**
	 * Used when focus of the destination is changed.
	 * 
	 * @param destination - the focus of the handler.
	 */
	public void setDestination(Destination destination)
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
	 * TODO
	 * Needs to implement a filter function that will determine if the new value is close enough to
	 * set the destination takeover
	 */
	public void valueListener()
	{
		//TODO might have to filter here instead
		_inputVal = _inputSource.getProgress();
		/**
		 * Used for Greatest Takes Presidence
		 */
		float destinationVal = _destination.getValue();
		
		//If the fader is in takeover mode, the destination gets the value of the input source
		//This only happens if the input source has changed since the last time
		if (_lastVal == _inputVal)
			_lastSimilar = false;
		else 
			_lastSimilar = true;
		
		 if(destinationVal == _inputVal)	
			 _takeOver = true;
		
		 // Sets the destination to True to mark that takeover is happening
		if(_lastSimilar && _takeOver)
		{
			_lastVal = _inputVal;
			setDestinationValue(_lastVal);
		}
		
		
	}
	
	/**
	 * For test purposes
	 * @return
	 */
	public float getSourceValue()
	{
		return _inputSource.getProgress();
	}

	/**
	 * For test purposes
	 * @return
	 */
	public float getDestinationValue()
	{
		return _destination.getValue();
	}

	/**
	 * For test purposes
	 */
	public void setSourceValue(int input)
	{
		_inputSource.setProgress(input);
		
	}
	/**
	 * Filters the value before setting it to the Destination
	 */
	public void setDestinationValue(float input)
	{
		int integerValue = _lpf.filterInput(input);
		
		_destination.setValue(integerValue);
		
	}

	public Destination getDestination() 
	{
		return _destination;
	}
}
