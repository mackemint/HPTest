/**
 * 
 */
package se.macke.AACS;

import java.util.Collections;
import java.util.LinkedList;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;


/**
 * @author macke
 *
 */
public class MySensorEventListener implements SensorEventListener
{
	/**
	 * The raw, unfiltered list of items from the sensor event
	 */
	public LinkedList<Float> rawValues = new LinkedList<Float>();

	public MySensorEventListener()
	{
		Log.i("object", "constructor");
	}

	/* 
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1)
	{
		// Edit this method, macke

	}

	/** 
	 * Overridden sensor method.
	 * @see getAccelerometer(event)
	 */
	@Override
	public void onSensorChanged(SensorEvent event)
	{
		int eventType = event.sensor.getType();

		switch(eventType)
		{
		case Sensor.TYPE_ACCELEROMETER:
			getAccelerometer(event);

			break;
		}
	}
	
	/**
	 * This handles a single event at a time and adds 
	 * to the raw values from the accelerometer. 
	 */

	private void getAccelerometer(SensorEvent event)
	{

		float accelerometer_z = event.values[2];

		rawValues.add(accelerometer_z);
	
	}

	/**
	 * 
	 * Gets float values from the accelerometer and returns them as a String array.
	 * 
	 * @return [0] as the value in 7 bit res, [1] as the array from the 
	 * accelerometer and [2] are the slope values
	 * 
	 */
	public int getVelocity()
	{

		int theInteger = 0;

		LinkedList<Float> filteredValues = lowPassFilter();
		LinkedList<Float> slopeValues = calculateSlopeValues(filteredValues);
		
		theInteger = sevenBitFormatting(slopeValues);

		rawValues.clear();
	
		return theInteger;
	}


	/**
	 * Picks out the greatest slope float and converts it into an int
	 * with resolution 0-127 
	 */
	private int sevenBitFormatting(LinkedList<Float> slopeValues)
	{
		
		int max = 127;
		double highest;
		
		if(!slopeValues.isEmpty())
			highest = Collections.max(slopeValues)*4;
	
		else
			highest = 10;
		
		int sevenBits = (int) Math.round(highest);
		
		if (sevenBits > max/1.6)
			sevenBits /= 1.6;
		

		if (sevenBits > max/1.3)
				sevenBits /= 1.3;

		if(sevenBits > max*1.1)
			sevenBits /= 1.2;
		
		if(sevenBits > max)
			sevenBits = max;
		
		return sevenBits;
	}

	/**
	 * Performs a low pass filtering of the rawValues LinkedList
	 * 
	 *  @return an array of low pass filtered values
	 *  
	 * @see http://blog.thomnichols.org/2011/08/smoothing-sensor-data-with-a-low-pass-filter
	 */
	private LinkedList<Float> lowPassFilter()
	{
		final float ALPHA = 0.2f;
		float filtered = 9.82f;
		float previous;
		float current;
		
		LinkedList<Float> filteredVals = new LinkedList<Float>();
		

		
		int theSize = rawValues.size();
		
		
		if (theSize>1)
		{
			for (int i = 0 ; i < theSize-1; i++)
			{
				previous = rawValues.get(i);
				current = rawValues.get(i+1);
				filtered = (float) previous*ALPHA + current*(1-ALPHA);
				filteredVals.add(filtered);
			}
		}
	
		
		return filteredVals;


	}


	/**
	 * Derives a list of items and adds them to the 
	 * list slopeValues
	 * 
	 * @see getVelocity
	 * 
	 * @return LinkedList of slope calculations
	 *  
	 */
	private LinkedList<Float> calculateSlopeValues(LinkedList<Float> listOfItems)
	{
		float current;
		float previous;
		float slope = 2.42f;

		/**
		 * The time between two events in SENSOR_DELAY_FASTEST mode
		 */
		float deltaT = 0.055f;


		LinkedList<Float> slopeValues = new LinkedList<Float>();


		int theSize = listOfItems.size();

		if(theSize > 1)
		{

			for (int i = 0; i < theSize-1; i++)
			{
				previous = listOfItems.get(i);
				current = listOfItems.get(i+1);
				slope = (Math.abs(current-previous))/deltaT;
				slopeValues.add(slope);

			}
		}

		return slopeValues;
	}

}
