package se.macke.hptest;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class HPMainActivity extends IOIOActivity 
{

	/**
	 * The top row is controlled by the bottom row
	 * TODO this will be replaced by analog inputs
	 */
	private SeekBar[] _bottomSeekRow;
	
	
	private Destination[] _destination;
	/**
	 * An input handler takes care of the communication between the faders
	 */
	private InputHandler[] _inputHandler;
	
	/**
	 * A pad listener only transmits the NoteOx message for 
	 * the corresponding button that is pressed.
	 */
	private PadListener _padListener;


	/**
	 * Performance pads
	 * There are 7 columns of these and 6 rows
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
	 * TODO change to MidiMessage
	 * The output queue containing Midi Messages
	 */
	private final ArrayBlockingQueue<Integer> out_queue = new ArrayBlockingQueue<Integer>(OUT_QUEUE_SIZE);
    private final static int OUT_QUEUE_SIZE = 100;
    
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

		_destination = new Destination[COLUMNS];
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

		_padListener = new PadListener(HPMainActivity.this);

		/**
		 * Setting up handlers for faders and onTouchListeners for pads
		 */
		for (int i = 0; i < COLUMNS; i++)
		{
			_destination[i] = new Destination(FADER_DEFAULT_CC + i, HPMainActivity.this);
//			_inputHandler[i] = new InputHandler(_bottomSeekRow[i],_destination[i]);


						
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
//           ShortMessage msg = new ShortMessage();

		int msg = note;
		Log.i(DEBUG_TAG,"Playing note " + note);
           try {
//                   msg.setMessage(ShortMessage.NOTE_ON, msg, vel);
                   out_queue.add(msg);
           } catch (Exception e) {
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
//      ShortMessage msg = new ShortMessage();

	int msg = cc;
	Log.i(DEBUG_TAG,"Changing CC#: " + cc);
      try {
//              msg.setMessage(ShortMessage.NOTE_ON, cc, val);
              out_queue.add(msg);
      } catch (Exception e) {
              Log.e(DEBUG_TAG,"InvalidMidiDataException caught");
      }
	}
	
	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class IOIO extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput led_;
		
		/**
		 * A class for handling fader and knob input
		 */
		private PotScanner _potScanner;
		/**
		 * A class for handling keyboard input
		 */
		private ButtonScanner _buttonScanner;
		
		

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
			led_ = ioio_.openDigitalOutput(0, true);
			
			_potScanner = new PotScanner(this.ioio_, _inputHandler);
			
			_buttonScanner = new ButtonScanner(this.ioio_, HPMainActivity.this);
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
		public void loop() throws ConnectionLostException {
			led_.write(!_performancePad[0][0].isPressed());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new IOIO();
	}
	
	
	
	
	
	
}
