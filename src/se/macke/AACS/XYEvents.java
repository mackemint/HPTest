/**
 * 
 */
package se.macke.AACS;

import android.view.MotionEvent;

/**
 *This class is used by activity to define modulation values.
 *
 */
public class XYEvents
{

	private float initialX = 0;
	private float initialY = 0;

	float lastX = 0;
	float lastY = 0;


	/**
	 * These may be set to different values at a later stage.
	 * 
	 * To get the correct offset of the pitch bend message
	 * MIDI pitch bend is an unsigned 14-bit integer with 8192 as center position
	 * 
	 * No idea why the values have to be doubled.
	 * There must be something wrong with the 
	 */
	int xNull = 8192;//*2;
	int yNull = 0;

	int xMin = 0;
	int yMin = 0;

	int xMax = 16383;//*2;
	int yMax = 127;
	
	
	private boolean _moved;


	/**
	 * Sets initial values when the finger touches a key.
	 * 
	 * @param event 		the motion event
	 */
	public void setInitial(MotionEvent event)
	{
		initialX = event.getX();
		initialY = event.getY();
	}

	/**
	 * Takes the motion event, checks if the threshold has 
	 * been reached and returns true or false
	 * 
	 * @param event 		the motion event
	 * 
	 * @return The first value in the boolean array is the check if the threshold is reached.
	 * The second value is if the finger is moving to the left or right.
	 */

	public boolean[] getXThreshold(MotionEvent event)
	{
		float deltaX = 0;
		int threshold = 8;
		int stopThreshold = 250;

		boolean[] returnValue = {false, false};

		deltaX = event.getX();

		float condition = (deltaX - initialX);

		//System.out.println(condition);
		/**
		 * When reaching the extreme values
		 * (anti flooding the output queue)
		 */
		if(condition >= stopThreshold || condition <= -stopThreshold)
		{
			returnValue[0] = false;
			returnValue[1] = false;
		}
		else if (condition > threshold)
		{
			returnValue[0] = true;
			returnValue[1] = true;
		}
		else if (condition < -threshold )
		{
			returnValue[0] = true;
			returnValue[1] = false;
		}
		/**
		 * If inside the threshold (no modulation)
		 */
		else if(condition <= threshold || condition >= -threshold)
		{
			returnValue[0] = false;
			returnValue[1] = false;
		}


		return returnValue;
	}

	/**
	 * Takes the motion event, checks if the threshold has 
	 * been reached and returns true or false.
	 * 
	 * @param event 		the motion event 
	 *
	 * @return The first value in the boolean array is the check if the threshold is reached.
	 * The second value is if the finger is moving to the left or right.
	 */
	public boolean[] getYThreshold(MotionEvent event)
	{
		float deltaY = event.getY();
		int threshold = 10;
		int stopThreshold = -265; 

		boolean[] returnValue = {false, false};

		float condition = (deltaY - initialY);

		/**
		 * When approaching the extreme values.
		 * This is to avoid sending unnecessary pitch bend messages.
		 */
		if(condition <= stopThreshold || condition >= 0)
		{
			returnValue[0] = false;
			returnValue[1] = false;
		}
		else if (condition > threshold)
		{
			returnValue[0] = true;
			returnValue[1] = true;
		}
		else if (condition < -threshold)
		{
			returnValue[0] = true;
			returnValue[1] = false;
		}
		else if(condition <= threshold || condition >= -threshold)
		{
			returnValue[0] = false;
			returnValue[1] = false;
		}

		return returnValue;
	}


	/**
	 * Actions performed when the finger starts to move around.
	 * Used by case: ACTION_MOVE in motionTracker method.
	 * 
	 * @param event	the motion event
	 * 
	 * @return Returns the values for the motionevents.
	 * [0] = x-values
	 * [1] = y-values
	 * 
	 */
	public int[] eventActions(MotionEvent event)
	{
		int xValue = 0;
		boolean xPositive = getXThreshold(event)[1];
		boolean xThresholdReached = getXThreshold(event)[0];

		int yValue = 0;
		boolean yPositive = getYThreshold(event)[1];
		boolean yThresholdReached = getYThreshold(event)[0];



		if(!xThresholdReached)
			xValue = xNull;

		if(!yThresholdReached)
			yValue = yNull;



		if(xThresholdReached)
			xValue = xHandleMove(event, xPositive);


		if(yThresholdReached)
			yValue = yHandleMove(event, yPositive);

		int[] value = {xValue, yValue};

		return value;


	}


	/**
	 * Converts the values from the readings from the screen and
	 * returns a value for pitch bend in 7 bit res.
	 * Used by eventActions 
	 * 
	 * @param event	what's going on on screen
	 * @param positive	if the finger is moving to the left or right
	 */
	private int xHandleMove(MotionEvent event, boolean positive)
	{
		/**
		 * to get the correct resolution of the returned float value
		 */
		int resolution = 70;

		int max = xMax;

		int min = xMin;

		/**
		 * To get the right offset if the user is doing a pitch down/up move
		 */
		float threshold = 10;

		float now;

		if(positive)
			now = event.getX() - threshold;
		else
			now = event.getX() + threshold;



		int pitchBendZero = xNull;

		/**
		 * To make sure the pitch bend doesn't start from fingers initial touch position, 
		 * but from the position where the pitch bend should start to take effect.
		 */
		float initial = initialX;



		float modulator = pitchBendZero;

		modulator += ((now-initial)*resolution);

		if (modulator >= max)
			modulator = max;


		else if (modulator <= min)
			modulator = min;


		return (int) modulator;


	}


	/**
	 * Returns a value for CC in MIDI.
	 * Used by eventActions 
	 * 
	 * @param event	what's going on on screen
	 * @param positive	if the finger is moving to the left or right
	 */
	private int yHandleMove(MotionEvent event, boolean positive)
	{

		/**
		 * to get the correct resolution of the returned float value
		 */
		float resolution = (float) 0.5;

		/**
		 * To get the right offset if the user is doing a pitch down/up move
		 */

		float threshold = 10;

		int max = yMax;

		int min = yMin;

		float now = 0; 

		if (positive)
			now = event.getY() - threshold;
		else if(!positive)
			now = event.getY() + threshold;


		/**
		 * To make sure the pitch bend doesn't start from the initial position, 
		 * but from the position where the pitch bend should start to take effect.
		 */

		float initial = initialY;

		float modulator;


		/**
		 * This will be returned as an array containing the actual values in MIDI.
		 */

		modulator = -(now-initial)*resolution;

		if(modulator > max)
			modulator = max;

		else if (modulator < min)
			modulator = min;

		return (int)modulator;

	}

	/**
	 * A method used to not flood the Queue with CC messages
	 * This checks to see that there is only a new message if the finger
	 * has moved more than one pixel from the last position.
	 * 
	 * @param event    the motion event
	 * 
	 * @return True or false depending
	 * on the difference from the last position of the finger.
	 */
	public boolean getLastX(MotionEvent event)
	{
		boolean position = false;

		float now = event.getX();
		int delta = 1;

		float condition = Math.abs(now - lastX);

		if (condition > delta)
		{
			lastX = now;
			position = true;
		}

		return position;
	}


	/**A method used to not flood the Queue with CC messages
	 * 
	 * @param event    the motion event
	 * 
	 * @return the difference from the last position of the finger 
	 */
	public boolean getLastY(MotionEvent event)
	{
		boolean position = false;
		int delta = 1;

		float now = event.getY();	

		float condition = Math.abs(now - lastY);

		if (condition > delta)
		{
			lastY = now;
			position = true;
		}

		return position;
	}


	/**
	 * Resets the last XY-coordinates when lifting the finger from the screen. 
	 */
	public void setLast(MotionEvent event)
	{
		lastX = 0;
		lastY = 0;

		initialX = 0;
		initialY = 0;

	}

	public void setMoved(boolean b) 
	{
		_moved = b;	
	}

	public boolean getMoved() 
	{
		return _moved;
	}

}
