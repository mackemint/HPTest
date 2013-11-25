
package se.macke.surfacetest;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.SpiMaster;
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
	private String DEBUG_TAG = STMainActivity.PROJECT_TAG + "PotScanner";
	
	private final String[] OUTPUT_DEBUG = {"A", "B", "C"};

	private final String[] INPUT_DEBUG ={"I1","I2","I3","I4","I5","I6"};
	/**
	 * Time in ms between cycles 
	 */
	private static final long PAUSETIME = 5;

	/**
	 * Used for pausing the thread
	 */
	boolean _running;

	/**
	 * IOIO from main activity
	 */
	private IOIO _ioio;

	/**
	 * The row pins for digital output
	 * 
	 */
	private final int ROW1_PIN = 28;
	private final int ROW2_PIN = 29;
	private final int ROW3_PIN = 30;

	
	/**
	 * The column pins for the analog input
	 *	
	 */
	private final int COL1_PIN = 31; 	//Grå
	private final int COL2_PIN = 44;	//Blå
	private final int COL3_PIN = 32;	//orange
	private final int COL4_PIN = 34;	//Gul
	private final int COL5_PIN = 46;	//Vit
	private final int COL6_PIN = 33;	//Grön






	/**
	 * Handles the connection with destination targets
	 */
	private InputHandler[][] _inputHandler;

	/**
	 * Handles the raw values from the analog input
	 */
	private LowPassFilter[][] _lpf;


	/**
	 * The colums take in the signal from the outputs
	 * Array containing columns of analog inputs
	 */
	private AnalogInput[] _analogInput;
	
	/**
	 * SPI master used to force sync between I/O
	 */
	private SpiMaster _spi;

	private final int misoPin = 3;
	
	private final int mosiPin = 4;
	
	private final int clkPin = 5;
	
	private final int[] ssPins = {8};
	
	private final byte[] _request = { 0x7f};

	private final byte[] _response = { 0x7f};

	/**
	 * Array of pins for analog input
	 */

	private final int[] _inPin = {COL1_PIN,COL2_PIN,COL3_PIN,COL4_PIN,COL5_PIN,COL6_PIN};

	
	

	/**
	 * The rows output signal
	 * Array containing rows of digital outputs
	 */
	private DigitalOutput[] _digitalOutput;

	/**
	 * Array of pins to act as a digital ground
	 */
//	private DigitalInput[] _gnd;

	/**
	 * Array of pins for digital output
	 */
	private int[] _outPin = {ROW1_PIN, ROW2_PIN,ROW3_PIN};
	


	private float[] _analogVal;

	private int _rowCount = 0;

	private int _smoothVal;




	/**
	 * Creates a new scanner with knowledge of the IOIO and the inputhandler form main activity
	 * 
	 * @param ioio_
	 * @param _inputHandler2
	 * @throws InterruptedException 
	 * @throws ConnectionLostException 
	 */
	public PotScanner(IOIO ioio_, InputHandler[][] inputHandler) throws InterruptedException, ConnectionLostException
	{

		Log.i(DEBUG_TAG, "Constructor");


		_ioio = ioio_;

		_running = true;

		_inputHandler = inputHandler;

		_lpf = new LowPassFilter[_outPin.length][_inPin.length];

		_analogVal = new float[_inPin.length];

		_analogInput = new AnalogInput[_inPin.length];
		
		
		_spi = ioio_.openSpiMaster(misoPin, mosiPin, clkPin, ssPins, SpiMaster.Rate.RATE_1M);

//		_gnd = new DigitalInput[_inPin.length];

		_digitalOutput = new DigitalOutput[_outPin.length];
		
		try
		{
			for (int i = 0; i < _outPin.length; i++)
			{

				/*
				 * As a workaround for the async bug, only the output is opened here	
				 * 
				 */
				Log.i(DEBUG_TAG, "Opening output" + i);
				
				_digitalOutput[i] = _ioio.openDigitalOutput(_outPin[i], DigitalOutput.Spec.Mode.NORMAL, false);

				for (int j = 0; j < _inPin.length; j++)
				{

					_analogInput[j] = _ioio.openAnalogInput(_inPin[j]); // removed to force syncing
					_lpf[i][j] = new LowPassFilter(_analogInput[j].read());

					_inputHandler[i][j].setInitial(_lpf[i][j].getPrevious());
					_analogInput[j].close();
				}


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
					scanColums();	
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

	/**
	 * Method for scanning all the analog inputs of a row.
	 * Opens one analog input at a time, reads the value and smooths it out for sending to the input handler.
	 * After reading an analog input, the pin is closed to force syncing between input and output.
	 * 
	 * @throws InterruptedException
	 * @throws ConnectionLostException
	 */
	private void scanColums() throws InterruptedException, ConnectionLostException 

	{
		_digitalOutput[_rowCount].write(true);
		
		_spi.writeRead(_request, _request.length, _request.length + _response.length, _response, _response.length);
		Log.i(DEBUG_TAG, "Sent SPI request");

		for (int i = 0; i < _inPin.length; i++)
		{
			
			
				Log.i(DEBUG_TAG,"Reading input: " + _inPin[i]);
				
				//Opening here to force matrix syncing
				_analogInput[i] = _ioio.openAnalogInput(_inPin[i]);
				Thread.sleep(PAUSETIME);
				
				_analogVal[i] = _analogInput[i].read();
				_smoothVal = _lpf[_rowCount][i].filterInput(_analogVal[i]);

				Log.i(DEBUG_TAG, "Input pin: " + INPUT_DEBUG[i] + " of output: " + OUTPUT_DEBUG[_rowCount] + " value is: " + _analogVal[i] + " smooth value is: " + _smoothVal);
				
				_inputHandler[_rowCount][i].setValue(_smoothVal);

				Log.i(DEBUG_TAG, "Finished smoothing");

				//Close after reading to force syncing 
				_analogInput[i].close();
			

				Thread.sleep(PAUSETIME);
				

		}
		_digitalOutput[_rowCount].write(false);
		
		countRows();
	}


	/**
	 * Counts up rows and wraps around when reaching the length
	 */
	private void countRows() 
	{
		Log.i(DEBUG_TAG,"Row counter is: " + _rowCount);
		_rowCount++;
	
		if (_rowCount == _outPin.length)
			_rowCount = 0;
	}

}
