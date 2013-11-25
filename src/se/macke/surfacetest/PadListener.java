package se.macke.surfacetest;

import android.graphics.LightingColorFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

/**
 * A pad listener only listens to pad values and is used 
 * for transmitting note on/off messages to the output queue.
 * 
 * The value of a pad is defined in the XML as a tag.
 * 
 * It also uses the accelerometer to approximate velocity input and touch events 
 * for X/Y control as well as aftertouch.
 * @author macke
 *
 */
public class PadListener implements OnTouchListener
{
	private static final int ACTIVE_COLOR = 0xFF00FF00;

	private static final String DEBUG_TAG = "PadListener";

	private final int INIT_COLOR = 0xFF000000;

	private int _padNumber = 0;

	STMainActivity _main;

	Button[][] _button;


	public PadListener(STMainActivity main, Button[][] b) 
	{
		_main = main;

		_button = b;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		Button thisButton = (Button) v;

		_padNumber = Integer.valueOf(String.valueOf(v.getTag()));

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

				releaseColumnMembers(thisButton);		

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

	/**
	 * Un-presses the buttons in the same column as the one pressed.
	 * 
	 * @param thisButton
	 */
	private void releaseColumnMembers(Button thisButton) 
	{
		int col = 0;
		int row = 0;

		//try to read the pads

			
		// for all pads do localization
		while(thisButton != _button[row][col])
		{
			System.out.println("<<<<col is:" + col);
			col++;
			
			if(col > _button.length)
			{

				System.out.println("<<<<row is:" + row);
					
				col = 0;
				row++;
			}
		}		
		
		
		

		//If it's a scene launch button
		if (col == _button.length)
		{
			System.out.println("Pressed a scene button!");
		
			for (int i = 0 ; i <= _button.length; i++)
			{

				System.out.println("Scene reset of col " + i);
			
				resetColumn(_button[row][i],i);
			}
			
		}

		resetColumn(thisButton, col);
	
	}

	private void resetColumn(Button b, int col) 
	{
		//Resets all buttons in the same column
		for (int i = 0; i < _button.length; i++)
		{
			if(_button[i][col] != b)
				_button[i][col].getBackground().setColorFilter(null);
		}

		//Gives the button a nice green tint
		b.getBackground().setColorFilter(new LightingColorFilter(INIT_COLOR,ACTIVE_COLOR));
		Log.i(DEBUG_TAG, "Finished resetting column " + col);
	}




}
