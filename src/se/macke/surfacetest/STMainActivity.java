package se.macke.surfacetest;

import ioio.javax.sound.midi.MidiMessage;
import ioio.javax.sound.midi.ShortMessage;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalOutput.Spec;
import ioio.lib.api.DigitalOutput.Spec.Mode;
import ioio.lib.api.Uart;
import ioio.lib.api.Uart.Parity;
import ioio.lib.api.Uart.StopBits;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import se.macke.hptest.R;


import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class STMainActivity extends IOIOActivity 
{


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
	 * A textview for viewing the last value recorded
	 */
	public TextView _textView;
	


	/**
	 * Performance pads
	 * There are 6 columns of these and 7 rows
	 * Column number 7 consists of scene launch buttons
	 */
//	private Button[][] _performancePad;


	/**
	 * Int for column switching
	 */
	private int _columnCounter;

	/**
	 * Int for row transposition
	 */
	private int _rowCounter;

	/**
	 * TODO change to 
	 * The output queue containing Midi Messages
	 */
	private final ArrayBlockingQueue<MidiMessage> out_queue = 
			new ArrayBlockingQueue<MidiMessage>(OUT_QUEUE_SIZE);

	private final static int OUT_QUEUE_SIZE = 200;

	/**
	 * Number of columns on the controller
	 */
	private final int COLUMNS = 6;

	private final int ROWS = 3;
	/**
	 * Start CC of column 1
	 */
	private final int FADER_DEFAULT_CC = 60;
	
	private SeekBar[][] _fader;


	private final static String DEBUG_TAG = "main";
	final static String PROJECT_TAG = "SurfaceTest";

	private void setupFaders()
	{
		_columnCounter = 0;

		_destinationProxy = new DestinationProxy[ROWS][COLUMNS];

		_inputHandler = new InputHandler[ROWS][COLUMNS];

//		_performancePad = new Button[COLUMNS][ROWS];

		_fader = new SeekBar[ROWS][COLUMNS];

		// Setting up on screen buttons

		_fader[0][0] = (SeekBar) findViewById(R.id.r0c0);
		_fader[0][1] = (SeekBar) findViewById(R.id.r0c1);
		_fader[0][2] = (SeekBar) findViewById(R.id.r0c2);
		_fader[0][3] = (SeekBar) findViewById(R.id.r0c3);
		_fader[0][4] = (SeekBar) findViewById(R.id.r0c4);
		_fader[0][5] = (SeekBar) findViewById(R.id.r0c5);

		
		_fader[1][0] = (SeekBar) findViewById(R.id.r1c0);
		_fader[1][1] = (SeekBar) findViewById(R.id.r1c1);
		_fader[1][2] = (SeekBar) findViewById(R.id.r1c2);
		_fader[1][3] = (SeekBar) findViewById(R.id.r1c3);
		_fader[1][4] = (SeekBar) findViewById(R.id.r1c4);
		_fader[1][5] = (SeekBar) findViewById(R.id.r1c5);

		_fader[2][0] = (SeekBar) findViewById(R.id.r2c0);
		_fader[2][1] = (SeekBar) findViewById(R.id.r2c1);
		_fader[2][2] = (SeekBar) findViewById(R.id.r2c2);
		_fader[2][3] = (SeekBar) findViewById(R.id.r2c3);
		_fader[2][4] = (SeekBar) findViewById(R.id.r2c4);
		_fader[2][5] = (SeekBar) findViewById(R.id.r2c5);
		

		_textView = (TextView) findViewById(R.id.textView);
		
		_textView.setText("1");
		
		


//		_padListener = new PadListener(STMainActivity.this, _performancePad);

		/**
		 * Setting up handlers for faders and onTouchListeners for pads
		 */
		for (int i = 0; i < ROWS; i++)
		{

			for (int j = 0; j < COLUMNS; j++)
			{
				_destinationProxy[i][j] = new DestinationProxy(_fader[i][j], STMainActivity.this);
//				_performancePad[i][j].setOnTouchListener(_padListener);
				_inputHandler[i][j] = new InputHandler(_destinationProxy[i][j]);
				
				

				_fader[i][j].setMax(127);
				_fader[i][j].setProgress(50);
				
				_fader[i][j].setOnSeekBarChangeListener(new OnSeekBarChangeListener()
				{
					
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						String strProg = "" + progress;
						Log.i("PROGRESS: ", strProg);
						_textView.setText(strProg);
						
					}
				});

			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_hpmain);

		setupFaders();

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

	 * Pushes the on screen button
	 * @param note
	 * @param vel
	 * @param i 
	 */
	public void addNoteToQueue(final int j,  final int i)
	{
		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
//				_performancePad[j][i].performClick();

			}
		});
	}

	/**
	 * Adds a CC value to the output queue
	 * 
	 * @param cc
	 * @param val
	 */
	public void addCcToQueue(final SeekBar fader, final int val)
	{


		Log.i(DEBUG_TAG,"Fader value set to " + val);

		runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				fader.setProgress(val);
//				_textView.setText(val);
				

			}
			


		});
	}

		/**
		 * This is the thread on which all the IOIO activity happens. 
		 */
		class IOIO extends BaseIOIOLooper 
		{
			private static final int BAUD = 31250;

			private static final int MIDI_OUTPUT_PIN = 7;

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
			 * A class for handling fader and knob input
			 */
			private PotScanner _potScanner;
			/**
			 * A class for handling keyboard input
			 */
			private ButtonScanner _buttonScanner;

			/**
			 * Thread for scanning the potentiometers
			 */
			Thread _potThread;

			/**
			 * Thread for scanning the button matrix
			 */
			Thread _buttonThread;



			/**
			 * Called every time a connection with IOIO has been established.
			 * Typically used to open pins.
			 * 
			 * @throws ConnectionLostException
			 *             When IOIO connection is lost.
			 * 
			 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
			 */
			@Override
			protected void setup() throws ConnectionLostException 
			{
				Log.i(DEBUG_TAG,"setup");

				led_ = ioio_.openDigitalOutput(0, false);



				try 
				{
					_potScanner = new PotScanner(this.ioio_, _inputHandler);
				} 
				catch (InterruptedException e) 
				{

					e.printStackTrace();
				}

				_buttonScanner = new ButtonScanner(this.ioio_, STMainActivity.this);

				_potThread = new Thread(_potScanner);

				_buttonThread = new Thread(_buttonScanner);

				_potThread.start();

				//			_buttonThread.start(); TODO not running right now

				//Initializing the output
				_midiOut = ioio_.openUart(null,new Spec(MIDI_OUTPUT_PIN,Mode.OPEN_DRAIN), 
						BAUD,Parity.NONE,StopBits.ONE);

				_outputStream = _midiOut.getOutputStream();


				Log.i(DEBUG_TAG,"setup finished");
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
//				for (int i = 0; i < _performancePad.length; i++)
//				{
//					for (int i1 = 0; i1 < _performancePad.length; i1++)
//
//						led_.write(!_performancePad[i][i1].isPressed());
//
//				}

				if (!out_queue.isEmpty())
				{
					try 
					{
						_outputStream.write(out_queue.poll().getMessage());
					} 
					catch (IOException e) 
					{
						Log.e(DEBUG_TAG,"Problem with MIDI output");
					}	
				}
				try 
				{
					Thread.sleep(10);
				} 
				catch (InterruptedException e) {
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
