
package se.macke.hptest;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import android.util.Log;

public class PotScanner implements Runnable
{

	
	private static final long PAUSETIME = 30;

	private final String DEBUG_TAG = "AnalogInput";
	
	boolean _running;
	
	private IOIO _ioio;
	
	private HPMainActivity _main;
	
	private AnalogInput _analogInput;
	
	private float _analogVal;
	
	private boolean _active;
	
	private float _lastVal;

	/**
	 * The input pin carries the property of the instance of this method
	 */
	private int ANALOG_INPUT_PIN;
	
	public PotScanner(HPMainActivity a, IOIO io, int input) 
	{
		Log.i(DEBUG_TAG, "Constructor");
		
		ANALOG_INPUT_PIN = input;
		
		_ioio = io;
		
		
		_main = a;

		_running = true;
		
		_analogVal = 0f;
		
//		_active = true;
		
		
		
		try
		{

			_analogInput = _ioio.openAnalogInput(ANALOG_INPUT_PIN);
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
				_analogVal = _analogInput.read();
				Thread.sleep(PAUSETIME);
				
				//Check to see if the value has changed since the last time
				if(_analogVal != _lastVal)
					setActive(true);
				
				//Highest takes presidence case
				if(_active)
					setText(_analogVal);
				
				_lastVal = _analogVal;
				
			} catch (InterruptedException e)
			{

				e.printStackTrace();
			} catch (ConnectionLostException e)
			{
				 _running = false;
				
				Log.i(DEBUG_TAG,"Exited App");
			}
			
			//sometimes do something else
			if(_running)
			{}
		}
	}


	private void setText(float floatVal)
	{
		
//		System.out.println("selected real value: " + floatVal);		
		
		_main.analogInfluence(floatVal,ANALOG_INPUT_PIN);
		
	}
	
	private void setActive(boolean a)
	{
		_active = a;
	}
	
	public void setTakePresidence(boolean t)
	{
		
	}
	
	

}
