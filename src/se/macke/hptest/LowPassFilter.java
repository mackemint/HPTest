package se.macke.hptest;

/**
 * The low pass filter does just that, smoothing of an input value from the IOIO 
 * and returns an integer number to be transmitted by MIDI.
 * 
 * @author macke
 *
 */
public class LowPassFilter 
{

	/**
	 * This takes a float from 0-1, multiplies it with 126 and returns an integer number
	 * @param input from the IOIO
	 * @return a filtered integer number
	 */
	public int filterInput(float input) 
	{
		int filtered = (int) (input * 126);
		
		return filtered;
	}

}
