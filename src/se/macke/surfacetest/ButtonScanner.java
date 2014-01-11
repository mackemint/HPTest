
package se.macke.surfacetest;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import android.util.Log;




/**
 * Scans hardware buttons and outputs messages to the output queue.
 * 
 * @author macke
 *
 */
public class ButtonScanner extends Thread
{

	
	private IOIO _ioio;
	/**
	 * An instance of the Main Activity to handle button presses
	 */
	private STMainActivity _main;

	/**
	 * What row is currently HIGH
	 */
	//		private int _rowCount = 0;


	/**
	 * Row pin numbers
	 */
	private final int ROW1_PIN = 18;
	private final int ROW2_PIN = 19;
	private final int ROW3_PIN = 20;
	private final int ROW4_PIN = 21;




	/**
	 * Row pin numbers
	 */
	private final int COL1_PIN = 25;
	private final int COL2_PIN = 24;
	private final int COL3_PIN = 23;
	private final int COL4_PIN = 22;

	/**
	 * Array of booleans used for storing previous states
	 * Rows output signal 
	 */
	private boolean[] row_;

	/**	 
	 * Array of booleans used for storing previous states
	 * Columns take input signal
	 */
	private boolean[] col_;

	/**
	 * Saves the current state as the previous one.
	 * Used for toggle button feature of H/W buttons
	 */
	private boolean[][] prevReading;

	/**
	 * The colums take in the signal from the outputs
	 * Array containing columns of digital inputs
	 */		
	private DigitalInput[] digitIn;

	/**
	 * Array of pins for digital input
	 */
	private int[] inPin = {COL1_PIN,COL2_PIN,COL3_PIN,COL4_PIN};

	/**
	 * The rows output signal
	 * Array containing rows of digital outputs
	 */
	private DigitalOutput[] digitOut;
	/**
	 * Array of pins for digital output
	 */
	private int[] outPin = {ROW1_PIN,ROW2_PIN,ROW3_PIN,ROW4_PIN};


	/**
	 * The time spent between every cycle
	 */
	int pauseTime = 3;

	private volatile boolean _running;

	/**
	 * The initial width and height of the button matrix
	 */
	private final int DEFAULT_LENGTH = outPin.length;

	/*
	 * Modulator buttons
	 */
	/**
	 * Shift button
	 */
	private final int[] MODIFIER_SHIFT = {1,2};
	private final int _SHIFT = 6;
	/**
	 * Solo modifier button
	 */
	private final int[] MODIFIER_SOLO = {2,3};
	private final int _SOLO = 6;
	/**
	 * Track on modifier
	 */
	private final int[] MODIFIER_MUTE = {3,0};
	private final int _MUTE = 6;

	/**
	 * Modifier toggle
	 */
	private Boolean _shift, _solo, _trackOn;

	/**
	 * Solo modifier button pressed returns this matrix
	 */
	private final int[][] SOLO_MATRIX ={{70,71,72,73},{74,75,6,7},{8,9,10,11},{12,13,14,15}};

	/**
	 * Track on modifier button pressed returns this matrix
	 */
	private final int[][] T_ON_MATRIX = {{80,81,82,83},{84,85,6,7},{8,9,10,11},{12,13,14,15}};
	
	private final int[][] SHIFT_MATRIX = {{90,91,92,93},{94,95,6,97},{98,99,100,11},{12,103,104,105}};

	/**
	 * Default set of notes transmitted to the output queue
	 */
	private int[][] MIDI_NOTE_NUMBER = {{0,1,2,3},{4,5,6,7},{8,9,10,11},{12,13,14,15}};

//	private HardwareButton[][] _hardwareButton;

	private String DEBUG_TAG = STMainActivity.PROJECT_TAG + "ButtonScanner";

	private DigitalOutput led_;


	/**
	 * Creates a new ButtonScanner with knowledge of main activity components.
	 * 
	 * @param ioio
	 * @param activity
	 * @param led 
	 * @throws ConnectionLostException
	 */
	public ButtonScanner(IOIO ioio, STMainActivity activity, DigitalOutput led) throws ConnectionLostException
	{
		//		Log.i(DEBUG_TAG , "Constructor" );

		_main = activity;

		_ioio = ioio;

		row_  = new boolean[DEFAULT_LENGTH]; 

		col_ = new boolean[DEFAULT_LENGTH];

		prevReading = new boolean[col_.length][row_.length];

		digitIn = new DigitalInput[inPin.length];

		digitOut = new DigitalOutput[outPin.length];

//		_hardwareButton = new HardwareButton[col_.length][row_.length];	TODO

		_running = true;


		led_ = led;

		_shift = false;
		_solo = false;
		_trackOn = false;

		
		
		


		for (int i = 0 ; i < col_.length ; i ++)
		{

			//			Log.i(DEBUG_TAG, "length i is: " + i);
			/**
			 * As a workaround for the async bug, only the output is opened here	
			 */
			digitOut[i] = _ioio.openDigitalOutput(outPin[i], false);

			/**
			 * Initializing the array of previous readings
			 */
			for (int j = 0; j < prevReading.length; j++)
			{
				//Initializing all hardware buttons with a Normal modifier
//				_hardwareButton[i][j] = new HardwareButton(_SHIFT, MIDI_NOTE_NUMBER[i][j],_SOLO,_MUTE,"n");	TODO
				prevReading[i][j] = false;

			}
			

		}
		/*
		 * Setting special modifiers to buttons with this property
		 */
//		_hardwareButton[1][2].setModifier("sh");
//		_hardwareButton[2][3].setModifier("s");
//		_hardwareButton[3][0].setModifier("m");
		
		
		//		Log.i(DEBUG_TAG, "Constructor finished setup" );

		//Increases the priority of the current thread
		//Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	}




	@Override
	public void run()
	{
		while(_running)
		{
			try
			{
				scanKeyboard();
			} 
			catch (ConnectionLostException e)
			{
				_running = false;
				e.printStackTrace();
			} 
			catch (InterruptedException e)
			{
				_running = false;			
				e.printStackTrace();
			}
			//While false, do nothing
			while(!_running);
		}	

	}

	/**
	 * Sets running status
	 * @param s - the status
	 */
	public void setStatus(boolean s)
	{
		_running = s;
	}

	/**
	 * Scans the button matrix and checks for interconnection between row and column
	 * If there is a connection, the corresponding hardwarebutton is pressed.
	 * 
	 * 
	 * @throws ConnectionLostException
	 * @throws InterruptedException
	 */
	private void scanKeyboard() throws ConnectionLostException, InterruptedException
	{
		//		Log.i(DEBUG_TAG, "initiated");
		for (int j = 0; j < row_.length; j++)
		{
			digitOut[j].write(true);

			for(int i = 0; i < col_.length; i++)
			{
				//				Log.i(DEBUG_TAG, "opened col: " + i);

				digitIn[i] = _ioio.openDigitalInput(inPin[i], 
						DigitalInput.Spec.Mode.PULL_DOWN);

//								Log.i(DEBUG_TAG, "reading col: " + i);

				//_hardwareButton[j][i].setCurrentState(digitIn[i].read());
				//performButtonPress(_hardwareButton[j][i]);

				Boolean current = digitIn[i].read();
				Boolean previous = prevReading[j][i];
				
				
				Log.i(DEBUG_TAG, "Current is:" + current + " and previous is: " + previous);

				
				performPress(j, i, current, previous);

				//Storing current state for comparison next loop
				prevReading[j][i] = current;


				/**
				 * This input is now closed to force syncing between I/O
				 */
				digitIn[i].close();

			}

			digitOut[j].write(false);

		}

	}



	/**
	 * Toggles state of currently pressed button
	 * 
	 * @param i - index of column
	 * @param j - index of row
	 * @param current - the current reading
	 * @param previous - previous reading
	 *  
	 * @throws ConnectionLostException
	 */
	private void performPress(int i, int j, Boolean current, Boolean previous)
			throws ConnectionLostException 
		{

		//* Note On
		//				Log.i(DEBUG_TAG, "row: " + j + " column: " + i);
		//		
		//				Log.i(DEBUG_TAG, "current: " + current);
		//				Log.i(DEBUG_TAG, "previous: " + previous);	


		Boolean keyDown = current && !previous;
		Boolean keyUp = !current && previous;

		//Note On, state goes from low to high
		if(keyDown)
		{
			handleMessage(i, j, true);

			led_.write(!current);

		}
		//* Note Off
		else if (keyUp)
		{
			handleMessage(i, j, false);

			led_.write(current);

		}
	}
	
	/**
	 * Compares hardwarebuttons' state with previous state.
	 * Handles message if state != previousState
	 * 
	 * pre: current button state is set
	 * post: previous state is set to current
	 * 
	 * @param button the hardware button object
	 */
//	private void performButtonPress(HardwareButton button)
//	{
//		
//		boolean current = button.getCurrentState();
//		boolean previous = button.getPreviousState();
//		
//		boolean keyDown = current && !previous;
	
	
//		// Only if state is toggled
//		if(keyDown || !keyDown)
//			handleButton(button,button.isPressed());
//		
//		button.setPreviousState(current);
//	}
	
	/**
	 * Handles button events.
	 * 
	 * pre: Button state is toggled
	 * post: handleMessage method is called according to conditions
	 * 
	 */
//	private void handleButton(HardwareButton button, boolean pressed)
//			throws ConnectionLostException 
//		{
//		
//		  mod = button.getModifier();
//	
// 		  setKeyboardModifier(mod, pressed);
//	
//		  if(_solo)		  
//			  handleMessage(button.getNoteNumber("s"),pressed);
//		else if(_trackOn)
//			  handleMessage(button.getNoteNumber("m"),pressed);
//		else if(_shift)
//			  handleMessage(button.getNoteNumber("sh"),pressed);
//		else
//			  handleMessage(button.getNoteNumber("n"),pressed);
//			
	
//		 
//
//	
//	}

	/**
	 * Handles what kind of MIDI note should be sent if modifiers are pressed.
	 * Takes a boolean as the third parameter for setting the message type.
	 * 
	 * @param i the column index of the note
	 * @param j the row index of the note
	 * @param noteCmd note On/Off
	 */
	private void handleMessage(int i, int j, Boolean noteCmd) 
	{
		int noteOn = 127;
		int noteOff = 0;

		/*
		 * When holding modifier buttons
		 */
		setKeyboardModifier(i, j,noteCmd);

		if(noteCmd)
			_main.addNoteToQueue(getRightNote(i,j),noteOn);
		else
			_main.addNoteToQueue(getRightNote(i,j),noteOff);
	}
	

/**
 * @param number - the MIDI Note message sent to output queue
 * @param keyDown - Note On of Note Off
 * 
 * Pre: keyboard modifier is set
 * post: note added to output queue
 */
//  private void handleMessage(int number, Boolean keyDown) 
//	{
//		int noteOn = 127;
//		int noteOff = 0;
//
//		if(keyDown)
//			_main.addNoteToQueue(number,noteOn);
//		else
//			_main.addNoteToQueue(number,noteOff);
//	}

	/**
	 * Sets memory of last button press.
	 * 
	 * If the button pressed is a modifier button, class members are set to true to perform modifier properties.
	 * 
	 * 
	 * 
	 * @param i - column index
	 * @param j - row index
	 * 
	 * @param noteOx - note On/Off
	 */
	private void setKeyboardModifier(int i, int j, Boolean noteOx) 
	{
		if (noteOx)
		{
			if(i == MODIFIER_SOLO[0] && j == MODIFIER_SOLO[1])
				_solo = true;
			else if(i == MODIFIER_MUTE[0] && j == MODIFIER_MUTE[1])
				_trackOn = true;
			else if(i == MODIFIER_SHIFT[0] && j == MODIFIER_SHIFT[1])
				_shift = true;
			

		}
		else
		{
			if(i == MODIFIER_SOLO[0] && j == MODIFIER_SOLO[1])
				_solo = false;
			else if(i == MODIFIER_MUTE[0] && j == MODIFIER_MUTE[1])
				_trackOn = false;
			else if(i == MODIFIER_SHIFT[0] && j == MODIFIER_SHIFT[1])
				_shift = false;
			


		}
	}

	/**
	 * pre: button is pressed
	 * post: keyboard modifier is set
	 * 
	 * @param modifier - the button pressed
	 * @param pressed - button up or down
	 */
//	private void setKeyboardModifier(String modifier, pressed) 
//	{
//		if (pressed)
//		{	
//			  if(modifier == "s")
//			  		_solo = true;
//			  
//			  else if(modifier == "m")
//			  		_trackOn = true;
//		}
//		else if (!pressed)
//		{
//			  if(modifier == "s")
//			  		_solo = false;
//			  else if(modifier == "m")
//			  		_trackOn = false;		
//		}
//	}
	

	/**
	 * Takes a hardwareButton as a parameter and returns a midi note number with respect to current modifier (if any)
	 * 
	 * @return the right matrix with or without modifier
	 */
	private int[][] getRightMIDIMatrix()
	{
		if(_solo)
			return SOLO_MATRIX;
		//return _hardwareButton.getNoteNumber("s");
		else if (_trackOn)
			return T_ON_MATRIX;
		else if (_shift)
			return SHIFT_MATRIX;
		//return _hardwareButton.getNoteNumber("m");
		return MIDI_NOTE_NUMBER;
		//return _hardwareButton.getNoteNumber("n");
	}

	/**
	 * Gets right value from matrix
	 * Uses row and column index to retrieve a message from a matrix of values
	 * 
	 * @param i - column index of note
	 * @param j - row index of note
	 * 
	 * @return the right note
	 */

	private int getRightNote(int i, int j)
	{

		return getRightMIDIMatrix()[i][j];

	}




	public void abort() {
		_running = false;
		interrupt();
	}



}


