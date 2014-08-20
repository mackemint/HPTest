package se.macke.AACS;

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
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MIDIBeatClock 
{

	private static final float COEFFICIENT = 0.11f;
	private long _timeBetweenTicks;
	private double _lastTimeBetweenTicks;

	private long _lastTick;
	private AACSmain _main;

	
	/**
	 * Will take an analog pulse class as instance variable for handling pulse events
	 * @param mainActivity
	 */
	public MIDIBeatClock(AACSmain mainActivity)
	{
		_timeBetweenTicks = 0;
		_main = mainActivity;
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
//			System.out.println("_timeBetweenTicks is: " + _timeBetweenTicks);
//
//			System.out.println("_lastTimeBetweenTicks is: " + _lastTimeBetweenTicks);

			System.out.println("Tempo is: " + getTempoInBPM());
//			_main.writeToTextView((getTempoInBPM()));

		}
		else
			_timeBetweenTicks = System.currentTimeMillis();


	}

	public double getTimeForSixteenthNote()
	{
		return (6*(_lastTimeBetweenTicks));
	}

	public double getTempoInBPM()
	{


		BigDecimal bd = new BigDecimal(60000/(4*getTimeForSixteenthNote()));
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();

	}

}
