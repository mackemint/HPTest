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
import android.widget.TextView;


/**
 * @author macke
 *
 */
public class ZSensorEventListener implements SensorEventListener
{
	/**
	 * The raw, unfiltered list of items from the sensor event
	 */
	public LinkedList<Float> filteredVals = new LinkedList<Float>();

	float _prevVal = 1;
	float _prevPrevVal = 1;

	/**
	 * Creates a sensor event listener that filters and compresses the input in a few stages.
	 */
	public ZSensorEventListener()
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
	 * to the filtered values from the accelerometer. 
	 */

	private void getAccelerometer(SensorEvent event)
	{
		float thisVal = event.values[2];
		_prevPrevVal = _prevVal;
		_prevVal = thisVal;

		float accelerometer_z = lpf(thisVal);
		filteredVals.add(accelerometer_z);

		if (filteredVals.size() > 20)
			filteredVals.removeFirst();

	}

	private float lpf(float thisVal) 
	{
		final float ALPHA = 0.2f;

		return (float) _prevVal*ALPHA + thisVal*(1-ALPHA) + _prevPrevVal*(ALPHA/2);

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
		return compressorAndLimiter(getSlopeFrom(filteredVals));

	}


	/**
	 * Picks out the greatest slope float and converts it into an int
	 * with resolution 0-127 
	 */
	private int compressorAndLimiter(Float float1)
	{

		int max = 127;
		
		double highest;
		
		float ratio = 1.6f;

		float threshold = (float) (max/ratio);

		highest = (float1)*ratio;

		int sevenBits = (int) Math.round(highest);

		if (sevenBits > threshold)
			sevenBits /= ratio;
		

		if(sevenBits > max)
			sevenBits = max;

		return sevenBits;
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
	private Float getSlopeFrom(LinkedList<Float> listOfItems)
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

		return Collections.max(slopeValues);
	}

}
