package se.macke.hptest;

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
	
	private static final int _MAX_RES = 126;

	private static final String DEBUG_TAG = "LowPassFilter";
	
	int _previousVal;

	/**
	 * Creates a new LowPassFilter with an initial value
	 * 
	 * @param initial
	 */
	public LowPassFilter(float initial)
	{
		Log.i(DEBUG_TAG,"Constructor");

		_previousVal = (int) (initial*_MAX_RES);
		
		Log.i(DEBUG_TAG,"Setting previous value to " + initial);

	}
	
	/**
	 * This takes a float from 0-1, multiplies it with 126 and returns an integer number
	 * @param input from the IOIO
	 * @return a filtered integer number
	 */
	public int filterInput(float input) 
	{

		Log.i(DEBUG_TAG,"Filtering input: " + input);
		
		int filtered = (int) (input * _MAX_RES);
		
		return compareVals(filtered);
	}

	private int compareVals(int val)
	{

		Log.i(DEBUG_TAG,"Comparing " + val + " with " + _previousVal);
		
		//Check to see if the value is swaying slightly
		if(val != _previousVal)//|| val != _previousVal+1)  || (val != _previousVal))
			_previousVal = val;
			
		return _previousVal;
			
	}
	
	/**
	 * 
	 * @return the previous value
	 */
	public int getPrevious()
	{
		return _previousVal;
	}

}
