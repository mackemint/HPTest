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
	private static final String PERFORM = "perform";

	private static final int ACTIVE_COLOR = 0xFF00FF00;

	private static final String DEBUG_TAG = "PadListener";

	private final int INIT_COLOR = 0xFF000000;

	private SensorEventListener _sensorEventListener;

	private SlideEventHandler _yEventHandler, _xEventHandler;

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

	private int _yCC;

	private Params _params;

	/**
	 * Sets up a new PadListener
	 * Has knowledge of all the buttons on the screen as well
	 * as the main activity where the MIDI is output.
	 * 
	 * @param main
	 * @param b
	 */
	public PadListener(AACSmain main, Params params, Button[][] b, SensorEventListener _eventListener, SlideEventHandler xHandler, SlideEventHandler yHandler) 
	{
		_main = main;

		_params = params;
		
		_button = b;

		_sensorEventListener = _eventListener;

		_xEventHandler = xHandler;
		_yEventHandler = yHandler;

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		Button thisButton = (Button) v;
		
		_noteMode = !_params.getMode().equalsIgnoreCase(PERFORM);

		int padNumber = Integer.valueOf(String.valueOf(v.getTag()));

		try
		{
			switch(event.getAction())
			{
			//Firstly only simple button presses and releases 
			case MotionEvent.ACTION_DOWN:

				initializeSlideEvents(event);

				if (!_noteMode && !_params.getBetaFlag())
					releaseColumnMembers(thisButton);		

				playNote(padNumber);

				break;

				
			case MotionEvent.ACTION_UP:

				releaseNote(padNumber);

				endSlide();
				resetPitchbend();

				break;

				//More sophisticated actions
			case MotionEvent.ACTION_MOVE:
				//Only do slide events in note mode.
				if (_noteMode)
					handleSlide(event);
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
	 * Handles slide events
	 * @param event
	 */
	private void handleSlide(MotionEvent event) 
	{
		handleX(event);
		handleY(event);
	}

	private void endSlide() 
	{
		_xEventHandler.reset();
		_yEventHandler.reset();
	}

	private void initializeSlideEvents(MotionEvent event) 
	{
		_yEventHandler.setInitial(event.getY());
		_xEventHandler.setInitial(event.getX());
	}



	private void handleX(MotionEvent event) 
	{
		int xMod = _xEventHandler.getModulationValue(event.getX());
		System.out.println("PitchBend set to: " + xMod);
		
		if(xMod != -1)
		{
			int data1 = xMod & 0x7f;
			int data2 = (xMod >> 7) & 0x7f; 
			xModulation(data1, data2);
		}
	}

	private void handleY(MotionEvent event) 
	{
		int data = _yEventHandler.getModulationValue(event.getY());
		if(data != -1)
			yModulation(data);

	}



	private void releaseNote(int padNumber) 
	{
		_main.addNoteToQueue(padNumber, 0);
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
	public boolean getNoteMode() 
	{
		return _noteMode;
	}

	/**
	 * @param _noteMode the _noteMode to set
	 */
	public void setNoteMode(boolean _noteMode) 
	{
		this._noteMode = _noteMode;
	}


	private void playNote(int padNumber) {
		int velocity = ((ZSensorEventListener) _sensorEventListener).getVelocity();
		_main.addNoteToQueue(padNumber, velocity);
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
//			System.out.println("<<<<col is:" + col);
			col++;

			if(col > _button.length)
			{

//				System.out.println("<<<<row is:" + row);

				col = 0;
				row++;
			}
		}		

		//If it's a scene launch button
		if (col == _button.length)
		{
			for (int i = 0 ; i <= _button.length; i++)
			{
				resetColumn(_button[row][i],i);
			}

		}

		resetColumn(thisButton, col);

	}

	/**
	 * Resets the pitch bend wheel after let go.
	 *  
	 */
	private void resetPitchbend()
	{
		int pitchBendZero = 8192;
		int dataByte1 = pitchBendZero & 0x7f;
		int dataByte2 = ((pitchBendZero >> 7) & 0x7f); //8
		_main.addPBToQueue(dataByte1, dataByte2); //dataByte1 needs to be LSB and dataByte2 MSB ....
		//		Log.i(DEBUG_TAG,"reset pitch bend" + dataByte2);

	}

	/**
	 * Used by motionTracker method when the finger moves over the screen
	 * on the x-axis.
	 * 
	 * Uses two variables that represents data byte 1 & 2 in the ShortMessage:
	 * @param d1		the MSB or CC#
	 * @param d2		the LSB (for aftertouch or pitchbend) or control value MSB 
	 * 
	 */
	protected void xModulation(int d1, int d2)
	{
		_main.addPBToQueue(d1, d2); 
	}

	/**
	 * Used by motionTracker method when the finger moves over the screen on the
	 * y-axis
	 * 
	 * @param d1		the MSB or CC#
	 * @param d2		the LSB (for aftertouch or pitchbend etc) or control value MSB
	 * 
	 */


	protected void yModulation(int d2)
	{
		_main.addCcToQueue(_yCC, d2);

	}


}
