package se.macke.hptest;

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
	public class ButtonScanner implements Runnable
	{
		private IOIO _ioio;
		/**
		 * An instance of the Main Activity to handle button presses
		 */
		private HPMainActivity _main;

		/**
		 * What row is currently HIGH
		 */
		private int _rowCount = 0;

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
		private final int COL1_PIN = 22;
		private final int COL2_PIN = 23;
		private final int COL3_PIN = 24;
		private final int COL4_PIN = 25;

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
		 * The notes transmitted to the output queue
		 */
		private int[][] _midiNoteNumber = {{0,1,2,3},{4,5,6,7},{8,9,10,11},{12,13,14,15}};

		/**
		 * The time spent between every cycle
		 */
		int pauseTime = 10;

		private volatile boolean _running;

		/**
		 * The initial width and height of the button matrix
		 */
		private final int DEFAULT_LENGTH = outPin.length;

		private final String DEBUG_TAG = "ButtonScanner";

		
		/**
		 * Creates a new ButtonScanner with knowledge of main activity components.
		 * 
		 * @param ioio
		 * @param activity
		 * @throws ConnectionLostException
		 */
		public ButtonScanner(IOIO ioio, HPMainActivity activity) throws ConnectionLostException
		{
			Log.i(DEBUG_TAG , "Constructor" );
			
			_main = activity;
			
			_ioio = ioio;
			
			row_  = new boolean[DEFAULT_LENGTH]; 

			col_ = new boolean[DEFAULT_LENGTH];

			prevReading = new boolean[col_.length][row_.length];

			digitIn = new DigitalInput[inPin.length];

			digitOut = new DigitalOutput[outPin.length];
			
			_running = true;


			for (int i = 0 ; i < col_.length ; i ++)
			{

				Log.i(DEBUG_TAG, "length i is: " + i);
				/**
				 * As a workaround for the async bug, only the output is opened here	
				 */
				digitOut[i] = _ioio.openDigitalOutput(outPin[i], false);

				/**
				 * Initializing the array of previous readings
				 */
				for (int j = 0; j < prevReading.length; j++)
					prevReading[i][j] = false;
			}
			Log.i(DEBUG_TAG, "Constructor finished setup" );
			
			//Increases the priority of the current thread
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		}




		@Override
		public void run()
		{
			while(_running)
			{
				try
				{
					scanButtons();
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
		 * If there is a connection, the corresponding togglebutton is pressed.
		 * 
		 * 
		 * @throws ConnectionLostException
		 * @throws InterruptedException
		 */
		private void scanButtons() throws ConnectionLostException, InterruptedException
		{
			Log.i(DEBUG_TAG, "initiated");

			digitOut[_rowCount].write(true);

			for(int i = 0; i < col_.length; i++)
			{
				Log.i(DEBUG_TAG, "opened col: " + i);

				digitIn[i] = _ioio.openDigitalInput(inPin[i], 
						DigitalInput.Spec.Mode.PULL_DOWN);

				Log.i(DEBUG_TAG, "reading col: " + i);
				Boolean current = digitIn[i].read();
				Boolean previous = prevReading[_rowCount][i];

				Log.i(DEBUG_TAG, "current: " + current);
				Log.i(DEBUG_TAG, "previous: " + previous);					

				/**
				 * currently high and previously low, toggle button
				 */
				if(current && !previous)
				{
					Log.i(DEBUG_TAG, "row: " + _rowCount + " column: " + i);
					
					_main.addNoteToQueue(_midiNoteNumber[_rowCount][i], 60);

				}		
				
				//For togglebutton-behaviour
				prevReading[_rowCount][i] = current;

				/**
				 * This input is now closed to force syncing between I/O
				 */
				digitIn[i].close();
				
			}

			digitOut[_rowCount].write(false);


			countRows();


		}


		/**
		 * Counts the rows
		 */
		private void countRows()
		{

			_rowCount++;

			if(_rowCount == row_.length)
				_rowCount = 0;


		}
		


	}


