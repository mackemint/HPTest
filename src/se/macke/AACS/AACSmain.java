package se.macke.AACS;

import ioio.javax.sound.midi.MidiMessage;
import ioio.javax.sound.midi.ShortMessage;
//import ioio.lib.api.CapSense;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalOutput.Spec;
import ioio.lib.api.DigitalOutput.Spec.Mode;
//import ioio.lib.api.DigitalInput.*;


import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff; 

public class AACSmain extends IOIOActivity 
{
	
	File file;
	/**
	 * A proxy class for MIDI I/O
	 */
	private DestinationProxy[][] _destinationProxy;
	/**
	 * An input handler takes care of the communication between the faders
	 * an the destination
	 */
	private InputHandler[][] _inputHandler;

	/**
	 * A pad listener only transmits the NoteOx message for 
	 * the corresponding button that is pressed.
	 */
	private PadListener _padListener;


	/**
	 * Performance pads
	 * There are 6 columns of these and 7 rows
	 * Column number 7 consists of scene launch buttons
	 */
	private Button[][] _performancePad;


	/**
	 * Int for column switching
	 */
	private int _columnCounter;

	/**
	 * Int for row transposition
	 */
	private int _rowCounter;

	/**
	 * The output queue containing Midi Messages
	 */
	private final ArrayBlockingQueue<MidiMessage> out_queue = 
			new ArrayBlockingQueue<MidiMessage>(OUT_QUEUE_SIZE);

	private final static int OUT_QUEUE_SIZE = 200;

	/**
	 * Number of columns on the controller
	 */
	private final int BUTTON_COLUMNS = 6;

	private final int BUTTON_ROWS = 7;

	/**
	 * Start CC of column 1
	 */
	private final int INIT_CC = 60;

	private int _midiChannel = 0;

	private final  String DEBUG_TAG = "main";

//	final static  String PROJECT_TAG = (String) getActivity().findViewById(R.string.app_name).toString();
	final static  String PROJECT_TAG = "AACS5.02";
	


	private static final int FADER_ROWS = 3;

	private static final int FADER_COLUMNS = 6; //TODO used to be 7, workaround for fader problem



	
	private void setupFaders()
	{
		_columnCounter = 0;

		_destinationProxy = new DestinationProxy[FADER_ROWS][FADER_COLUMNS];

		_inputHandler = new InputHandler[FADER_ROWS][FADER_COLUMNS];

		_performancePad = new Button[BUTTON_COLUMNS][BUTTON_ROWS];

		// Setting up on screen buttons

		_performancePad[0][0] = (Button) findViewById(R.id.r0c0);
		_performancePad[0][1] = (Button) findViewById(R.id.r0c1);
		_performancePad[0][2] = (Button) findViewById(R.id.r0c2);
		_performancePad[0][3] = (Button) findViewById(R.id.r0c3);
		_performancePad[0][4] = (Button) findViewById(R.id.r0c4);
		_performancePad[0][5] = (Button) findViewById(R.id.r0c5);
		_performancePad[0][6] = (Button) findViewById(R.id.r0c6);

		_performancePad[1][0] = (Button) findViewById(R.id.r1c0);
		_performancePad[1][1] = (Button) findViewById(R.id.r1c1);
		_performancePad[1][2] = (Button) findViewById(R.id.r1c2);
		_performancePad[1][3] = (Button) findViewById(R.id.r1c3);
		_performancePad[1][4] = (Button) findViewById(R.id.r1c4);
		_performancePad[1][5] = (Button) findViewById(R.id.r1c5);
		_performancePad[1][6] = (Button) findViewById(R.id.r1c6);

		_performancePad[2][0] = (Button) findViewById(R.id.r2c0);
		_performancePad[2][1] = (Button) findViewById(R.id.r2c1);
		_performancePad[2][2] = (Button) findViewById(R.id.r2c2);
		_performancePad[2][3] = (Button) findViewById(R.id.r2c3);
		_performancePad[2][4] = (Button) findViewById(R.id.r2c4);
		_performancePad[2][5] = (Button) findViewById(R.id.r2c5);
		_performancePad[2][6] = (Button) findViewById(R.id.r2c6);

		_performancePad[3][0] = (Button) findViewById(R.id.r3c0);
		_performancePad[3][1] = (Button) findViewById(R.id.r3c1);
		_performancePad[3][2] = (Button) findViewById(R.id.r3c2);
		_performancePad[3][3] = (Button) findViewById(R.id.r3c3);
		_performancePad[3][4] = (Button) findViewById(R.id.r3c4);
		_performancePad[3][5] = (Button) findViewById(R.id.r3c5);
		_performancePad[3][6] = (Button) findViewById(R.id.r3c6);

		_performancePad[4][0] = (Button) findViewById(R.id.r4c0);
		_performancePad[4][1] = (Button) findViewById(R.id.r4c1);
		_performancePad[4][2] = (Button) findViewById(R.id.r4c2);
		_performancePad[4][3] = (Button) findViewById(R.id.r4c3);
		_performancePad[4][4] = (Button) findViewById(R.id.r4c4);
		_performancePad[4][5] = (Button) findViewById(R.id.r4c5);
		_performancePad[4][6] = (Button) findViewById(R.id.r4c6);

		_performancePad[5][0] = (Button) findViewById(R.id.r5c0);
		_performancePad[5][1] = (Button) findViewById(R.id.r5c1);
		_performancePad[5][2] = (Button) findViewById(R.id.r5c2);
		_performancePad[5][3] = (Button) findViewById(R.id.r5c3);
		_performancePad[5][4] = (Button) findViewById(R.id.r5c4);
		_performancePad[5][5] = (Button) findViewById(R.id.r5c5);
		_performancePad[5][6] = (Button) findViewById(R.id.r5c6);

		_padListener = new PadListener(AACSmain.this, _performancePad);

		/**
		 * Setting up TouchListeners for pads
		 */
		for (int i = 0; i < BUTTON_COLUMNS; i++)
		{
			for (int j = 0; j < BUTTON_ROWS; j++)
			{
				_performancePad[i][j].setOnTouchListener(_padListener);
			}
		}

		//Counter for cc values, increases for every position
		int ccCounter = 0;

		// Setting up Proxys and Handlers for fader input
		for (int i = 0; i < FADER_ROWS; i++)
		{

			for (int j = 0; j < FADER_COLUMNS; j++)
			{
				_destinationProxy[i][j] = new DestinationProxy(INIT_CC + ccCounter, AACSmain.this);	//TODO fader problem -1

				_inputHandler[i][j] = new InputHandler(_destinationProxy[i][j]);

				ccCounter++;
			}

		}


	}

	/**
	 * Creates a keyboard layout for the pad surface
	 * 
	 * @param b
	 */
	private void setKeyboardLayout(final boolean b) 
	{

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				int whiteKeyColor = 0xFFFFFFFF;
				int blackKeyColor = 0xFF808080;
//				int normalColor = 0xFF000000;

				//Setting the keyboard
				if (b)	
				{
//					_midiChannel = 1;
					_padListener.setNoteMode(true);

					//Button rows are backwards
					for (int i = 0; i < BUTTON_COLUMNS; i++)
					{
						//Button col
						for (int j = 0; j< BUTTON_ROWS; j++)
						{
							System.out.printf("index of i is: %d and j is: %d \n" , i , j);

							_performancePad[i][j].getBackground().setColorFilter(whiteKeyColor, PorterDuff.Mode.MULTIPLY);

							if(i%2 == 0) // Black keys
							{
								if (j == 1 || j == 3 || j == 6 && i != 0)
									_performancePad[i][j].getBackground().setColorFilter(blackKeyColor, PorterDuff.Mode.MULTIPLY);
							}
							else if(i%2 != 0)
							{
								if (j == 0 || j == 2 || j == 4)
									_performancePad[i][j].getBackground().setColorFilter(blackKeyColor, PorterDuff.Mode.MULTIPLY);
							}

						}
					}
				}
				//Resetting the keyboard
				else if (!b)
				{
					_midiChannel = 0;
					_padListener.setNoteMode(false);
					for (int i = 0; i < BUTTON_COLUMNS; i++)
					{
						for (int j = 0; j< BUTTON_ROWS; j++)
						{
							_performancePad[i][j].getBackground().setColorFilter(null);

						}
					}
				}
			}
		});

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Sets orientation to both sides Landscape
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		setContentView(R.layout.activity_hpmain);

		// Starting up, the system bar fades out
		View rootView = getWindow().getDecorView();
		rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE); 

		setupFaders();
		//		setKeyboardLayout(true);
		
		file = new File(this.getFilesDir(), "readBuffered.txt");
		

	}

	//TODO add pause, resume methods
	@Override
	protected void onPause()
	{
		super.onPause();

	}


	//TODO check if this works
	@Override
	protected void onResume()
	{
		super.onResume();


	}


	/**
	 * Sets column to param
	 * @param c - integer value
	 */
	public void setColumnCounter(int c) 
	{
		_columnCounter = c;
	}

	/**
	 * 
	 * @return the column counter
	 */
	public int getColumnCounter()
	{
		return _columnCounter;
	}

	/**
	 * Sets row to param
	 * @param c - integer value
	 */
	public void setRowCounter(int c) 
	{
		_rowCounter = c;
	}

	/**
	 * 
	 * @return the row counter
	 */
	public int getRowCounter()
	{
		return _rowCounter;
	}

	/**
	 * Adds a MIDI note to the output queue
	 * 
	 * @param note
	 * @param vel
	 */
	public void addNoteToQueue(int note, int vel)
	{
		ShortMessage msg = new ShortMessage();

		Log.i(DEBUG_TAG,"Note is: " + note + " trying to change layout...");


		if (note == 91) //MIDI Channel set to 2
		{
		
			setKeyboardLayout(true);
			Log.i(DEBUG_TAG,"Set note mode");
		}
		else if (note == 90)
		{
			setKeyboardLayout(false);
			Log.i(DEBUG_TAG,"Disabled note mode");		
		}
		//Only note mode for pads
		if(_padListener.getNoteMode())
		{
			if (note > 23 && note < 66)
				_midiChannel = 1;
			else
				_midiChannel = 0;
		}


		//		int msg = note;
		if (vel>0)
			Log.i(DEBUG_TAG,"Note " + note + " on with velocity:" + vel);
		else
			Log.i(DEBUG_TAG,"Note " + note + " off");
		try 
		{
			msg.setMessage(ShortMessage.NOTE_ON, _midiChannel , note, vel);
			out_queue.add(msg);
		} 
		catch (Exception e) 

		{
			Log.e(DEBUG_TAG,"InvalidMidiDataException caught");
		}
	}

	/**
	 * Adds a CC value to the output queue
	 * 
	 * @param cc
	 * @param val
	 */
	public void addCcToQueue(int cc, int val)
	{
		ShortMessage msg = new ShortMessage();
		
		int ccMIDICh = 0;

		Log.i(DEBUG_TAG,"Changing CC#: " + cc + " to " + val);
		try 
		{
			msg.setMessage(ShortMessage.CONTROL_CHANGE, ccMIDICh, cc, val);
			out_queue.add(msg);
		} 

		catch (Exception e) 

		{
			Log.e(DEBUG_TAG,"InvalidMidiDataException caught");
		}
	}

	/**
	 * This is the thread on which all the IOIO activity happens. 
	 */
	class IOIO extends BaseIOIOLooper 
	{
		private static final int BAUD = 31250;

		private static final int MIDI_OUTPUT_PIN = 7;
		private static final int MIDI_INPUT_PIN = 6;

		private final ioio.lib.api.DigitalInput.Spec INPUT = new ioio.lib.api.DigitalInput.Spec(MIDI_INPUT_PIN);

		/** The on-board LED. */
		private DigitalOutput led_;

		/**
		 * The output for MIDI messages
		 */
		private Uart _midiOut;

		/**
		 * The stream used for sending the MIDI bytes
		 */
		private OutputStream _outputStream;

		/**
		 * A class for handling keyboard input
		 */
		private ButtonScanner _buttonScanner;

		/**
		 * A class for handling fader and knob input
		 */
		private PotScanner _potScanner;



		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * @throws InterruptedException 
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException, InterruptedException 
		{


			Log.i(DEBUG_TAG,"setup");

			led_ = ioio_.openDigitalOutput(0, false);

			_potScanner = new PotScanner(this.ioio_, _inputHandler, file, AACSmain.this);

			_buttonScanner = new ButtonScanner(this.ioio_, AACSmain.this, led_);


			_potScanner.start();
			
//			_buttonScanner.start();

			//Initializing the output
			_midiOut = ioio_.openUart(new DigitalInput.Spec(MIDI_INPUT_PIN,DigitalInput.Spec.Mode.PULL_DOWN),
					new Spec(MIDI_OUTPUT_PIN,Mode.NORMAL),BAUD,Parity.NONE,StopBits.ONE);

			_outputStream = _midiOut.getOutputStream();


			//Increases the priority of the current thread
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			
			Log.i(DEBUG_TAG,"setup finished");
		} 

		/**
		 * Kills the threads
		 */
		@Override
		public void disconnected()
		{
			try{

				_potScanner.abort();
				_buttonScanner.abort();
			}
			catch (NullPointerException e)
			{
				System.out.println("Nullpointer error");
			}
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException 
		{
			//			for (int i = 0; i < _performancePad.length; i++)
			//			{
			//				for (int j = 0; j < _performancePad.length; j++)
			//
			//					led_.write(!_performancePad[i][j].isPressed());
			//
			//			}

			if (!out_queue.isEmpty())
			{
				try 
				{
					_outputStream.write(out_queue.poll().getMessage());
					led_.write(false);
					Thread.sleep(1);
					led_.write(true);
				} 
				catch (IOException e) 
				{
					Log.e(DEBUG_TAG,"Problem with MIDI output");
				} 
				catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}

		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() 
	{
		return new IOIO();
	}






}
