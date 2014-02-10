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
	
	private static final int _MAX_RES = 125;
	
	private final int MULTIPLIER = 156;

	private static final String DEBUG_TAG = AACSmain.PROJECT_TAG + "LowPassFilter";
	
	//The filtered value for comparison
	float _fVal;

	/**
	 * Filter coefficient value
	 */
	private final float coef_ = 0.6f;

	private int _intVal;
	
	int _lpfCounter;
	
	/**
	 * Creates a new LowPassFilter with an initial value
	 * 
	 * @param initial
	 * @param _lpfCounter 
	 */
	public LowPassFilter(float initial, int row, int lpfCounter)
	{
		Log.i(DEBUG_TAG,"Constructor");
		
		//Correcting wrong wiring of potentiometers
	
		initial = getRightPolarity(initial, row);

//		_previousVal = (int) (initial*_MAX_RES);
		_fVal =  initial;
		
		_lpfCounter = lpfCounter;
		
//		Log.i(DEBUG_TAG,"Setting previous value to " + initial);

	}

	/**
	 * Method for returning the right polarity of the pots that I accidently miswired.
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
	 * @param input from the IOIO
	 * @return a filtered integer number
	 */
	public int filterInput(float input, int row) 
	{

		Log.i(DEBUG_TAG,"Filtering input: " + input);
		
		input = getRightPolarity(input, row);
		
		_fVal = input * (1.f - coef_) + _fVal * coef_;
//		_fVal = input;
		
		_intVal = (int) (_fVal * MULTIPLIER);
		

		Log.i(DEBUG_TAG,"LPF" + _lpfCounter + " input: " + input + " _fVal: " + _fVal + " _intVal: " + _intVal);

		return assertVal(_intVal);
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
