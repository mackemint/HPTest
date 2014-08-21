
package se.macke.AACS;

public class SlideEventHandler
{

	//	final int X_INIT = 8192;

	//	final int X_MAX = 16383;	

	private int _modulationValue;
	private float _prevVal;

	private int _max;
	private final int MIN_VAL = 0;
	private int _offset;
	private int _threshold;
	private int _thresholdCounter;
	private boolean _invert;
	private float _resolution;

	/**
	 * Creates a new SlideEventHandler with a max value and an offset 
	 * 
	 * @param max - the maximum value for the parameter
	 * @param offset - offset value 
	 * @param threshold - value before the counter starts to move
	 * @param invert -  true if invert value
	 * @param r - resolution of slide event
	 */
	public SlideEventHandler(int max, int offset, int threshold, boolean invert,float r)
	{
		_max = max;
		_offset = offset;
		_threshold = threshold;
		_invert = invert;
		_resolution = r;

	}

	/**
	 * Sets initial values when the finger touches the screen.
	 * 
	 * @param event 		the motion event
	 */
	public void setInitial(float init)
	{
		_prevVal = init;
		_modulationValue += _offset;
	}

	/** 
	 * @param f - where the finger is on the screen right now
	 * @return modulation value, -1 if invalid
	 */
	public int getModulationValue(float f) 
	{
		int val = getCounter(f);

		if(thresholdNotReached())
			return -1;

		return val;
	}

	private boolean thresholdNotReached() 
	{
		return Math.abs(_thresholdCounter) <= _threshold;
	}

	/**
	 * Pre: Finger is moving on screen, initial 
	 * Post: Returns a value between 0 and 127
	 * 
	 * @param event	what's going on on screen
	 * @return -1 if threshold not reached to avoid unnecessary flooding of MIDI output
	 * 
	 */
	private int getCounter(float now)
	{
		int val = 0;
		val = needsInvert(getDiff(now));

		_thresholdCounter += val;

		if(Math.abs(_thresholdCounter) > _threshold)
			_modulationValue += val;

		if (_modulationValue > (_max))
		{	
			_modulationValue = _max;
			return -1;
		}
		else if (_modulationValue < (MIN_VAL))
		{	
			_modulationValue = MIN_VAL;
			return -1;
		}
		return (int)_modulationValue;
	}

	private int needsInvert(int diff) 
	{
		if (_invert)
			return -diff;
		return diff;
	}
	
	
	
	

	/**
	 * 
	 * @param now where the finger is on screen
	 * @return the change in offset compared the the last position, negative if moving downward.
	 */
	private int getDiff(float now) 
	{	
		float diff = Math.round(Math.abs((now-_prevVal)*_resolution));
		boolean bigger = now > _prevVal;
		_prevVal = now;
		if(bigger)
			return (int)diff;	
		return (int)-diff;
	}

	/**
	 * Resets counters
	 */
	public void reset() 
	{
		_thresholdCounter = 0;
		if (_offset != 0)
			_modulationValue = MIN_VAL;
	}


}
