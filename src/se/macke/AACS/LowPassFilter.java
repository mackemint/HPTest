package se.macke.AACS;

import android.util.Log;

/**
 * The low pass filter does just that, smoothing of an input value from the IOIO 
 * and returns an integer number to be transmitted by MIDI.
 * 
 * @author macke
 *
 */
public class LowPassFilter 
{
	
	private static final int _MAX_RES = 127;
	
	private final int MULTIPLIER = 156;

	private static final String DEBUG_TAG = AACSmain.PROJECT_TAG + "LowPassFilter";
	
	/**
	 * Raw float value from A/D conversion
	 */
	float _fVal;
	/**
	 * Integer value in 7 bit res
	 */
	private int _intVal;

	/**
	 * Filter coefficient value
	 */
	private final float coef_ = 0.13f;

	
	/**
	 * 		//Just a counter for debugging
	 */
	int _lpfCounter;
	
	/**
	 * Creates a new LowPassFilter with an initial value
	 * 
	 * @param initial - float value at initialization
	 * @param _lpfCounter 
	 */
	public LowPassFilter(float initial, int row, int lpfCounter)
	{
		Log.i(DEBUG_TAG,"Constructor");
		
		//Correcting wrong wiring of potentiometers
		initial = getRightPolarity(initial, row);


		// Initializing raw values for filtering
		_fVal =  initial;
		_intVal = adConversion(_fVal);
		

		_lpfCounter = lpfCounter;
		

	}

	/**
	 * Method for returning the right polarity of the pots that I accidently.
	 * 
	 * @param val  analog value
	 * @param row  the row 0 and 1 are wrong wired
	 * 
	 * @return value with the right polarity
	 */
	private float getRightPolarity(float val, int row) 
	{
		if (row < 2)
			return val = (1- (val + 0.19f));
		
		return val;
	}
	
	/**
	 * This takes a float from 0-1, multiplies it with a constant and returns an integer number
	 * @param float input value from the IOIO
	 * @return a filtered integer number in 7 bit res
	 */
	public int filterInput(float input, int row) 
	{

		input = getRightPolarity(input, row);
		
		_fVal = input * (1.f - coef_) + _fVal * coef_;

		int thisInt = adConversion(_fVal);
		
		_intVal = (int) (thisInt * (1.f - coef_) + _intVal * coef_);

		return assertVal(_intVal);
	}

	/**
	 * Converts a float number from A/D to 7 bit resolution for MIDI implementation
	 * 
	 * @param val - filtered analog float val
	 * @return an int in 7 bit res
	 */
	private int adConversion(float val) 
	{
		return (int) (val * MULTIPLIER);
	}

	/**
	 * Assertion that the value is in the right scope
	 * 
	 * @param the input
	 * 
	 * @return a value between 0 - 127
	 */
	private int assertVal(int val)
	{


		if (val > _MAX_RES)
			return _MAX_RES;
		if (val < 0)
			return 0;
					
		return val;
			
	}
	
	/**
	 * 
	 * @return the int value
	 */
	public int getIntValue()
	{
		return _intVal;
	}

}
