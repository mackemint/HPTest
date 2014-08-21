package se.macke.AACS;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import android.annotation.TargetApi;
import android.net.NetworkInfo.DetailedState;
import android.os.Build;
import android.util.Log;


/**
 * MIDI uses timing messages with a peroidicity of 24 pulses per quarter note.
 * One quarter note has 4 sixteenth notes. This gives 6 pulses per sixteenth note.
 * 
 * The time between two ticks multiplied by 6 gives us the time for a sixteenth note.
 * 
 * The time will have to be measured continually between every incoming tick.
 * 
 * Set a default length calculated by 120 BPM
 * For every new tick, add the difference from the previous one to the old time and divide by two.
 * 
 * An output message of the time of a sixteenth note and the BPM will be displayed in the console.
 */

public class MIDIBeatClock extends Thread
{

	private static final String DEBUG_TAG = "MIDI Beat clock";
	/**
	 * 
	 */
	private static final int GATE_OUTPUT_PIN = 10;
	private long _timeBetweenTicks;
	private double _lastTimeBetweenTicks;

	private long _lastTick;
	private boolean _running;

	private IOIO _ioio;
	private DigitalOutput _pulseOutput;


	/**
	 * Will take an analog pulse class as instance variable for handling pulse events
	 * @param ioio_ 
	 */
	public MIDIBeatClock(IOIO ioio_)
	{
		_timeBetweenTicks = 0;
		_ioio = ioio_;

		Log.i(DEBUG_TAG ,"Midi beat clock constructor");


		try {
			_pulseOutput = _ioio.openDigitalOutput(GATE_OUTPUT_PIN);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_running = false ;
	}

	public void tick() 
	{
		if (_timeBetweenTicks != 0)
		{
			long thisTick = System.currentTimeMillis();

			long timeDiff = thisTick - _lastTick;

			_lastTick = thisTick;

			_timeBetweenTicks = timeDiff;

			_lastTimeBetweenTicks = (_timeBetweenTicks + _lastTimeBetweenTicks)/2;

			//			double firstFiltering =  (_timeBetweenTicks* (1.f- COEFFICIENT) + _lastTimeBetweenTicks*COEFFICIENT);
			//			
			//			_lastTimeBetweenTicks =  (firstFiltering* (1.f- COEFFICIENT) + _timeBetweenTicks*COEFFICIENT);

			//			System.out.println("thisTick is: " + thisTick);
			//
			System.out.println("_timeBetweenTicks is: " + _timeBetweenTicks);
			//
			System.out.println("_lastTimeBetweenTicks is: " + _lastTimeBetweenTicks);

			System.out.println("Tempo is: " + getTempoInBPM());
			//			_main.writeToTextView((getTempoInBPM()));

		}
		else
			_timeBetweenTicks = System.currentTimeMillis();


	}
	@Override
	public void run()
	{
		while(true)
		{

			if(_running)
			{
				long bananer = getTimeForSixteenthNote();
				setGatePulsWidth(bananer);
				Log.i(DEBUG_TAG ,"running gate sequence with time: " + bananer);
			}
		}

	}

	private void setGatePulsWidth(long pulsWidthInMillis) 
	{
		
		try 
		{
			long openGate = (long) (pulsWidthInMillis -(pulsWidthInMillis * 0.1));
			long closedGate = (long) (pulsWidthInMillis * 0.1);
			_pulseOutput.write(true);
			Thread.sleep(openGate);
			_pulseOutput.write(false);
			Thread.sleep(closedGate);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private long getTimeForSixteenthNote()
	{
		Log.i(DEBUG_TAG ,"ticks gate time is: " + 6*(_lastTimeBetweenTicks));
		if (_lastTimeBetweenTicks < 10)
			return 120;
		else if (_lastTimeBetweenTicks > 300)
			return 300;

		return (long) (6*(_lastTimeBetweenTicks));
	}

	private double getTempoInBPM()
	{


		BigDecimal bd = new BigDecimal(60000/(4*getTimeForSixteenthNote()));
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();

	}

	public void setRunningStatus(boolean b) 
	{

		Log.i(DEBUG_TAG ,"running status set to " + b);
		_running = b;
	}
	public void abort() 
	{
		_running = false;
				interrupt();
	}
}
