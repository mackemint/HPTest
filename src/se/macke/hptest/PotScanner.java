
package se.macke.hptest;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import android.util.Log;


/**
 * This is a runnable class that is used for scanning analog inputs.
 * It takes input from the IOIO and uses the InputHandler from the main activity
 * to send to the destination.
 * 
 * This will be a 3x6 matrix, where there are 3 outputs and 6 inputs.
 * 
 * @author macke
 *
 */
public class PotScanner implements Runnable
{
	private final String DEBUG_TAG = "PotScanner";

	/**
	 * Time in ms between cycles 
	 */
	private static final long PAUSETIME = 30;

	/**
	 * Used for pausing the thread
	 */
	boolean _running;

	/**
	 * IOIO from main activity
	 */
	private IOIO _ioio;

	/**
	 * The column pins for the analog input
	 *	TODO check IO
	 */
	private final int COL1_PIN = 37;
	private final int COL2_PIN = 38;
	private final int COL3_PIN = 39;
	private final int COL4_PIN = 40;
	private final int COL5_PIN = 41;
	private final int COL6_PIN = 42;


	/**
	 * The row pins for digital output
	 * 
	 * TODO check appropriate I/O
	 */
	private final int ROW1_PIN = 41;
	private final int ROW2_PIN = 42;
	private final int ROW3_PIN = 43;




	/**
	 * Handles the connection with destination targets
	 */
	private InputHandler[] _inputHandler;

	/**
	 * Handles the raw values from the analog input
	 */
	private LowPassFilter[] _lpf;


	/**
	 * The colums take in the signal from the outputs
	 * Array containing columns of analog inputs
	 */
	private AnalogInput[] _analogInput;

	/**
	 * Array of pins for analog input
	 */

	private final int[] _inPin = {COL1_PIN};//,COL2_PIN,COL3_PIN,COL4_PIN,COL5_PIN,COL6_PIN};


	/**
	 * The rows output signal
	 * Array containing rows of digital outputs
	 */
	private DigitalOutput[] _digitalOutput;


	/**
	 * Array of pins for digital output
	 */
	private int[] _outPin = {ROW1_PIN,ROW2_PIN,ROW3_PIN};


	private float[] _analogVal;

	private int _rowCount = 0;


	/**
	 * Creates a new scanner with knowledge of the IOIO and the inputhandler form main activity
	 * 
	 * @param ioio_
	 * @param inputHandler
	 * @throws InterruptedException 
	 */
	public PotScanner(IOIO ioio_, InputHandler[] inputHandler) throws InterruptedException 
	{

		Log.i(DEBUG_TAG, "Constructor");


		_ioio = ioio_;

		_running = true;

		_inputHandler = inputHandler;

		_lpf = new LowPassFilter[_inPin.length];

		_analogVal = new float[_inPin.length];

		_analogInput = new AnalogInput[_inPin.length];

		_digitalOutput = new DigitalOutput[_outPin.length];

		try
		{
			for (int i = 0; i < _inPin.length; i++)
			{

				/*
				 * As a workaround for the async bug, only the output is opened here	
				 * 
				 *_analogInput[i] = _ioio.openAnalogInput(_inPin[i]); // removed to force syncing
				 */
				if(i%2 == 0)	//Only half the amount of outputs
					_digitalOutput[i] = _ioio.openDigitalOutput(_outPin[i], false);

				_lpf[i] = new LowPassFilter(_analogInput[i].read());

				_inputHandler[i].setInitial(_lpf[i].getPrevious());
			}
		} 
		catch (ConnectionLostException e)
		{
			_running = false;
			e.printStackTrace();
		}
		Log.i(DEBUG_TAG, "Constructor finished");	
	}


	@Override
	public void run()
	{

		Log.i(DEBUG_TAG, "Run method");
		//always do something
		while(_running)
		{

			try
			{
				for (int i = 0 ; i < _outPin.length; i++)
				{
					_digitalOutput[_rowCount ].write(true);

					scanColums();
					
					_rowCount++;
					
					if (_rowCount == _outPin.length)
						_rowCount = 0;
						
				}

			} 
			
			
			catch (InterruptedException e)
			{

				e.printStackTrace();
			} 
			catch (ConnectionLostException e)
			{
				_running = false;

				Log.i(DEBUG_TAG,"Exited App");
			}

			//sometimes do something else
			if(!_running)
			{}
		}
	}


	private void scanColums() throws InterruptedException,
	ConnectionLostException 

	{
		for (int i = 0; i < _inPin.length; i++)
		{

			Log.i(DEBUG_TAG,"Reading input: " + i);

			_analogVal[i] = _analogInput[i].read();

			Log.i(DEBUG_TAG, "Finished reading");

			/* 
			 * 	In case the input reads even if it shouldn't for some reason,
			 *  it only reports to the handler if the value is greater than 0.
			 */
			
			if(_analogVal[i] > 0)
			{
				int smoothVal = _lpf[i].filterInput(_analogVal[i]);

				_inputHandler[i].setValue(smoothVal);
				Log.i(DEBUG_TAG, "Finished smoothing");

			}
			else
				Log.i(DEBUG_TAG, "Value is too low!");

		}
		Thread.sleep(PAUSETIME);
	}

}
