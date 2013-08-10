
package se.macke.hptest;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import android.util.Log;


/**
 * This is a runnable class that is used for scanning analog inputs.
 * It takes input from the IOIO and uses the InputHandler from the main activity
 * to send to the destination.
 * 
 * 
 * @author macke
 *
 */
public class PotScanner implements Runnable
{


	private static final long PAUSETIME = 30;

	private final String DEBUG_TAG = "PotScanner";

	boolean _running;

	private IOIO _ioio;

	private InputHandler[] _inputHandler;

	private LowPassFilter[] _lpf;

	/**
	 * The input pin carries the property of the instance of this method
	 */
	private final int ANALOG_INPUT_PIN1 = 37;
	private final int ANALOG_INPUT_PIN2 = 38;

	private final int[] _INPIN = {ANALOG_INPUT_PIN1};

	private AnalogInput[] _analogInput;

	private float[] _analogVal;


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

		_lpf = new LowPassFilter[_INPIN.length];

		_analogVal = new float[_INPIN.length];
		
		_analogInput = new AnalogInput[_INPIN.length];

		try
		{
			for (int i = 0; i < _INPIN.length; i++)
			{
				_analogInput[i] = _ioio.openAnalogInput(_INPIN[i]);
				
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
				for (int i = 0; i < _INPIN.length; i++)
				{

					Log.i(DEBUG_TAG,"Reading input: " + i);
					
					_analogVal[i] = _analogInput[i].read();

					Log.i(DEBUG_TAG, "Finished reading");
					
					
					int smoothVal = _lpf[i].filterInput(_analogVal[i]);
					
					Log.i(DEBUG_TAG, "Finished smoothing");
					
					_inputHandler[i].setValue(smoothVal);

				}
				Thread.sleep(PAUSETIME);


			} catch (InterruptedException e)
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

}
