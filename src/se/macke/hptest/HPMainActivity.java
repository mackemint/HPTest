package se.macke.hptest;

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


import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;

public class HPMainActivity extends IOIOActivity 
{


	/**
	 * A proxy class for MIDI I/O
	 */
	private DestinationProxy[] _destinationProxy;
	/**
	 * An input handler takes care of the communication between the faders
	 * an the destination
	 */
	private InputHandler[] _inputHandler;

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

	private final int ROWS = 7;
	/**
	 * Start CC of column 1
	 */
	private final int FADER_DEFAULT_CC = 60;

	private final static String DEBUG_TAG = "main";

	private void setupFaders()
	{
		_columnCounter = 0;

		_destinationProxy = new DestinationProxy[COLUMNS];

		_inputHandler = new InputHandler[COLUMNS];

		_performancePad = new Button[COLUMNS][ROWS];

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

		_padListener = new PadListener(HPMainActivity.this, _performancePad);

		/**
		 * Setting up handlers for faders and onTouchListeners for pads
		 */
		for (int i = 0; i < COLUMNS; i++)
		{
			_destinationProxy[i] = new DestinationProxy(FADER_DEFAULT_CC + i, HPMainActivity.this);

			_inputHandler[i] = new InputHandler(_destinationProxy[i]);



			for (int j = 0; j < ROWS; j++)
			{
				_performancePad[i][j].setOnTouchListener(_padListener);
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
	 * Adds a MIDI note to the output queue
	 * 
	 * @param note
	 * @param vel
	 */
	public void addNoteToQueue(int note, int vel)
	{
		ShortMessage msg = new ShortMessage();

		//		int msg = note;
		Log.i(DEBUG_TAG,"Playing note " + note);
		try 
		{
			msg.setMessage(ShortMessage.NOTE_ON, note, vel);
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

		//		int msg = cc;
		Log.i(DEBUG_TAG,"Changing CC#: " + cc + " to " + val);
		try 
		{
			msg.setMessage(ShortMessage.NOTE_ON, cc, val);
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

			_buttonScanner = new ButtonScanner(this.ioio_, HPMainActivity.this);

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
			for (int i = 0; i < _performancePad.length; i++)
			{
				for (int i1 = 0; i1 < _performancePad.length; i1++)

					led_.write(!_performancePad[i][i1].isPressed());

			}
			
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
