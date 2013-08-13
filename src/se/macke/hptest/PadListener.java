package se.macke.hptest;

import android.graphics.LightingColorFilter;
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

	private final int INIT_COLOR = 0xFF000000;

	private int _padNumber = 0;

	HPMainActivity _main;

	Button[][] _button;


	public PadListener(HPMainActivity main, Button[][] b) 
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

				//Gives the button a nice green tint
				thisButton.getBackground().setColorFilter(new LightingColorFilter(INIT_COLOR,ACTIVE_COLOR));

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

		outerLoop: // Steps through rows
			for (int i = 0 ; i < _button.length; i++)
			{
				System.out.println(">row: " + i);

				for (int j = 0; j <= _button.length; j++)
				{
					System.out.println(">>>col: " + j);

					if(_button[i][j] == thisButton)
					{
						col = j;//Saving the column number for nulling the others

						System.out.printf(">>>>>>>Pressed at row: %d,  col: %d\n",i,j);

						break outerLoop;

					}
				}
			}

		//Resets all buttons in the same column
		for (int i = 0; i <_button.length; i++)
		{
			if(_button[i][col] != thisButton)
			{
				_button[i][col].getBackground().setColorFilter(null);

			}
		}

	}




}
