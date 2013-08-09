package se.macke.hptest;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * A pad listener only listens to pad values and is used 
 * for transmitting note on/off messages to the output queue.
 * 
 * The value of a pad is defined in the XML as a tag.
 * 
 * @author macke
 *
 */
public class PadListener implements OnTouchListener
{
	private int _padNumber = 0;
	HPMainActivity _main;
	
	public PadListener(HPMainActivity main) 
	{
		_main = main;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		_padNumber =  Integer.valueOf(String.valueOf(v.getTag()));
		
		/**
		 * Will be set by the accelerometer
		 */
		int velocity = 60;
		
		try
		{
			switch(event.getAction())
			{
			case MotionEvent.ACTION_DOWN:

				System.out.println("Pressed button: " + _padNumber);
				_main.addNoteToQueue(_padNumber, velocity);

				break;

			case MotionEvent.ACTION_UP:


				System.out.println("Released button: " + _padNumber);
				_main.addNoteToQueue(_padNumber, 0);
				
				break;
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		return false;
	}

}
