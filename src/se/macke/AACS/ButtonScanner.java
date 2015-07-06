
package se.macke.AACS;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;


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
	private AACSmain _main;

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


	/**
	 * Modifier toggle
	 */
	private Boolean _shift, _solo, _trackOn;

	private Params _params = new Params();

	/**
	 * Solo modifier button pressed returns this matrix
	 */
	private final int[][] SOLO_MATRIX = _params.getSoloValues();

	/**
	 * Track on modifier button pressed returns this matrix
	 */
	private final int[][] T_ON_MATRIX = _params.getMuteValues();

	private final int[][] SHIFT_MATRIX = _params.getShiftValues();

	/**
	 * Default set of notes transmitted to the output queue
	 */
	private int[][] MIDI_NOTE_NUMBER = _params.getNoteValues();

	/**
	 * A HardwareButton on the keyboard
	 */
	private HardwareButton[][] _hardwareButton; 

	private String DEBUG_TAG = AACSmain.PROJECT_TAG + "ButtonScanner";

	/**
	 * Creates a new ButtonScanner with knowledge of main activity components.
	 * 
	 * @param ioio
	 * @param activity
	 * @param led 
	 * @throws ConnectionLostException
	 */
	public ButtonScanner(IOIO ioio, AACSmain activity, DigitalOutput led) throws ConnectionLostException
	{
		_main = activity;

		_ioio = ioio;

		row_  = new boolean[DEFAULT_LENGTH]; 

		col_ = new boolean[DEFAULT_LENGTH];

		prevReading = new boolean[col_.length][row_.length];

		digitIn = new DigitalInput[inPin.length];

		digitOut = new DigitalOutput[outPin.length];

		_hardwareButton = new HardwareButton[col_.length][row_.length];

		_running = true;

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
			digitIn[i] = _ioio.openDigitalInput(inPin[i], DigitalInput.Spec.Mode.PULL_DOWN);

			/**
			 * Initializing the array of previous readings
			 */
			for (int j = 0; j < prevReading.length; j++)
			{
				//Initializing all hardware buttons with a Normal modifier

				_hardwareButton[i][j] = new HardwareButton(SHIFT_MATRIX[i][j], MIDI_NOTE_NUMBER[i][j],
						SOLO_MATRIX[i][j],T_ON_MATRIX[i][j],"n");	

				prevReading[i][j] = false;

			}

		}
		/*
		 * Setting special modifiers to buttons with this property
		 */
		_hardwareButton[1][2].setModifier("sh");
		_hardwareButton[1][3].setModifier("s");	
		_hardwareButton[2][0].setModifier("m");
	}




	@Override
	public void run()
	{
		while(_running)
		{
			try
			{
				scanKeyboard();
				Thread.sleep(1);
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
			_ioio.sync();	

			for(int i = 0; i < col_.length; i++)
			{

				_hardwareButton[j][i].setCurrentState(digitIn[i].read());
				isButtonToggled(_hardwareButton[j][i]);

			}

			digitOut[j].write(false);

		}

	}

	/**
	 * Compares hardwarebuttons' state with previous state.
	 * Handles message if state != previousState
	 * 
	 * pre: current button state is set
	 * post: previous state is set to current
	 * 
	 * @param the hardware button object
	 * @throws ConnectionLostException 
	 */
	private void isButtonToggled(HardwareButton button) throws ConnectionLostException
	{

		// Only if state is toggled
		if(button.isPressed() || button.isReleased())
			toggleButton(button);

		button.setPreviousState(button.getCurrentState());
	}

	/**
	 * Handler for button toggle events.
	 * 
	 * pre: Button state is toggled
	 * post: handleMessage method is called according to conditions
	 * 
	 */
	private void toggleButton(HardwareButton button)
			throws ConnectionLostException 
			{

		String mod = button.getModifier();
		boolean pressed = button.getCurrentState();

		setKeyboardModifier(mod, pressed);

		if(_solo)		  
			handleMessage(button.getNoteNumber("s"),pressed);
		else if(_trackOn)
			handleMessage(button.getNoteNumber("m"),pressed);
		else if(_shift)
			handleMessage(button.getNoteNumber("sh"),pressed);
		else
			handleMessage(button.getNoteNumber("n"),pressed);
			}

	/**
	 * @param number - the MIDI Note message sent to output queue
	 * @param keyDown - Note On or Note Off
	 * 
	 * Pre: keyboard modifier is set
	 * post: note added to output queue
	 */
	private void handleMessage(int number, Boolean keyDown) 
	{
		int noteOn = 1;
		int noteOff = 0;

		if(keyDown)
			_main.addNoteToQueue(number,noteOn);
		else
			_main.addNoteToQueue(number,noteOff);
	}

	/**
	 * pre: button is pressed
	 * post: keyboard modifier is set
	 * 
	 * @param modifier - modifier of the button pressed
	 * @param pressed - button up or down
	 */
	private boolean setKeyboardModifier(String modifier, boolean pressed) 
	{
		if (pressed)
		{	
			if(modifier == "sh")
				return _shift = true;

			else if(modifier == "s")
				return _solo = true;

			else if(modifier == "m")
				return _trackOn = true;
		}
		else if (!pressed)
		{
			if(modifier == "sh")
				return _shift = false;

			else if(modifier == "s")
				return _solo = false;
			else if(modifier == "m")
				return _trackOn = false;		
		}
		return false;
	}

	public void abort() 
	{
		_running = false;
		interrupt();
	}



}


