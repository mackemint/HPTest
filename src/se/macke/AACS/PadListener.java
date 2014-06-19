package se.macke.AACS;

import android.graphics.LightingColorFilter;
import android.hardware.SensorEventListener;
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
 * It uses the accelerometer to approximate velocity input and touch events 
 * for X/Y control as well as aftertouch.
 * @author macke
 *
 */
public class PadListener implements OnTouchListener
{
	private static final int ACTIVE_COLOR = 0xFF00FF00;

	private static final String DEBUG_TAG = "PadListener";

	private final int INIT_COLOR = 0xFF000000;
	
	private SensorEventListener _sensorEventListener;

	/**
	 * Pad numbers are contained as tags in the layout
	 */
	//	private int _padNumber = 0;

	AACSmain _main;

	Button[][] _button;

	/**
	 * Used if the control surface is set to Note mode
	 */
	private boolean _noteMode = false;


	/**
	 * Sets up a new PadListener
	 * Has knowledge of all the buttons on the screen as well
	 * as the main activity where the MIDI is output.
	 * 
	 * @param main
	 * @param b
	 */
	public PadListener(AACSmain main, Button[][] b, SensorEventListener _eventListener) 
	{
		_main = main;

		_button = b;
		
		_sensorEventListener = _eventListener;

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		Button thisButton = (Button) v;

		int padNumber = Integer.valueOf(String.valueOf(v.getTag()));
		
		try
		{
			switch(event.getAction())
			{
			//Firstly only simple button presses and releases 
			case MotionEvent.ACTION_DOWN:

				//				System.out.println("Pressed button: " + _padNumber);

				if (!_noteMode)
					releaseColumnMembers(thisButton);		

				int velocity = ((MySensorEventListener) _sensorEventListener).getVelocity();
				_main.addNoteToQueue(padNumber, velocity);

				break;

			case MotionEvent.ACTION_UP:

				//				System.out.println("Released button: " + _padNumber);

				_main.addNoteToQueue(padNumber, 0);

				break;

				//More sophisticated actions
			case MotionEvent.ACTION_MOVE:

				//TODO implement motion events from piano project
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

	/**
	 * @return the  note mode
	 */
	public boolean getNoteMode() {
		return _noteMode;
	}

	/**
	 * @param _noteMode the _noteMode to set
	 */
	public void setNoteMode(boolean _noteMode) {
		this._noteMode = _noteMode;
	}




}
