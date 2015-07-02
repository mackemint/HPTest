package se.macke.AACS;

/**
 * Class to handle button presses on keyboard matrix.
 * Subclass to Keyboard that contains memory classes for shift,solo and mute.
 * 
 * A hardware button has two states, current and previous to handle from what and to what 
 * state it is currently traversing.
 * It also carries midi note numbers for sending messages.
 *
 * @author macke
 *
 */
public class HardwareButton 
{
	/**
	 * Used for handling button presses
	 */
	private boolean _previousState,_currentState;
	
	/**
	 * Note number.
	 * Modified when pressing solo or mute.
	 */
	private int _noteNumber, _soloNumber, _muteNumber,_shiftNumber;
	
	/**
	 * Modifier toggle
	 */
	private String _modifier;

	/**
	 * 
	 * Creates a new HardwareButton object
	 * 
	 * 
	 * @param note - regular note number
	 * @param solo - solo mod note number
	 * @param mute - mute mod note number
	 * @param modifier - n for normal, s for solo, m for mute,sh for shift
	 */
	public HardwareButton(int shift, int note, int solo, int mute, String modifier)
	{

		
		_modifier = modifier;
		
		_shiftNumber = shift;
		_noteNumber = note;
		_soloNumber = solo;
		_muteNumber = mute;
		
	}

	/**
	 * Sets button modifier parameter. Normally used only once while initializing.
	 * 
	 * @param modifier
	 */
	public void setModifier(String modifier)
	{
		this._modifier = modifier;	
	}
	
	/**
	 * Used when the button pressed has special properties, such as being a shift, solo or mute modifier.
	 * 
	 * @return button modifier String
	 * 
	 */
	public String getModifier()
	{
		return this._modifier;
	}


	/**
	 * @return the _previousState
	 */
	public boolean getPreviousState() 
	{
		return _previousState;
	}


	/**
	 * @param previous sets button previous state
	 */
	public void setPreviousState(boolean previous) 
	{
		this._previousState = previous;
	}


	/**
	 * @return the current state
	 */
	public boolean getCurrentState() 
	{
		return _currentState;
	}


	/**
	 * @param current the _currentState to set
	 */
	public void setCurrentState(boolean current) 
	{
		this._currentState = current;
	}

	/**
	 * Takes a parameter for returning the correct number
	 * 
	 * @param modifier s for solo, m for mute, sh for shift
	 *   
	 * @return the _noteNumber
	 */
	public int getNoteNumber(String modifier) 
	{

		if (modifier == "s")
			return _soloNumber;
		else if (modifier == "m")
			return _muteNumber;
		else if (modifier == "sh")
			return _shiftNumber;
		
		return _noteNumber;
	}

	public boolean isPressed() {

		if (_currentState && !_previousState)
			return true;
		
		return false;
	}
	public boolean isReleased()
	{
		if(!_currentState && _previousState)
			return true;
		
		return false;
	}
}
