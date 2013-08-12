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
	private static final int SCENE_INACTIVE_COLOR = 0xFF000000;

	private static final int INACTIVE_COLOR = 0x00000000;

	private static final int ACTIVE_COLOR = 0xFF00FF00;

	private static final String BUTTON_STRING = "►";

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

				System.out.println("Reset other buttons.");

				thisButton.setTextColor(ACTIVE_COLOR);
				
				thisButton.getBackground().setColorFilter(new LightingColorFilter(0xFF7f7f7f, ACTIVE_COLOR));
				

				System.out.println("Set message and color");

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

		try
		{
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

							if (j == _button.length)
								throw new ArrayIndexOutOfBoundsException();
							
//							thisButton.setText(BUTTON_STRING);
							
							break outerLoop;

						}
					}
				}

		for (int i = 0; i <_button.length; i++)
		{
			_button[i][col].setTextColor(INACTIVE_COLOR);	
			_button[i][col].getBackground().setColorFilter(new LightingColorFilter(0xFF404040,0xff7f7f7f));
		}


		}
		
		//I made this as an exception originally mostly just for fun
		catch(ArrayIndexOutOfBoundsException aie)
		{
			System.out.println("Pressed a scene launch button!");
			for (int i = 0; i <_button.length; i++)
			{
				_button[i][col].setTextColor(SCENE_INACTIVE_COLOR);

			}

		}


	}

}
