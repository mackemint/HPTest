package se.macke.AACS;



import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class MidiIn extends Thread
{
	private InputStream inputStream;

	BufferedReader _br;
	/**
	 * Channels used for MIDI input
	 */
	private Uart midi_in_;

	private AACSmain _main;


	private boolean _running;

	private final String DEBUG_TAG = "MIDI_IN: ";

	private IncomingMIDIHandler _incomingMIDIHandler;

//	private MIDIBeatClock _midiBeatClock;

	public MidiIn(MIDIBeatClock _midiBeatClock, Uart MIDI, AACSmain m_) throws ConnectionLostException, InterruptedException
	{

		Log.i(DEBUG_TAG  ,"Constructor");

		_main = m_;

		midi_in_ = MIDI;

		inputStream = midi_in_.getInputStream();
		
		_incomingMIDIHandler = new IncomingMIDIHandler(_midiBeatClock, MidiIn.this);

		_running  = true;

		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		Log.i(DEBUG_TAG  ,"Constructor finished");
	}




	@Override
	public void run()
	{
		while(_running)
		{
			try
			{
				int midiByte;

				midiByte = inputStream.read();

				Log.i(DEBUG_TAG, String.format("reading: %02x", midiByte));

				_incomingMIDIHandler.add(midiByte);

				Thread.sleep(2);

			} 
			catch (IOException e) 
			{
				
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void abort() 
	{
		_running = false;
		interrupt();
	}

	/**
	 * A call to the main activity to set the color of a pad
	 * 
	 * @param noteNumber
	 * @param velocityColor
	 */
	public void setPadColor(int noteNumber, int velocityColor)
	{	
		Log.i(DEBUG_TAG, String.format("note: %d triggered with color value: %d", noteNumber, velocityColor));
		_main.setColorOfPad(noteNumber, velocityColor);
	}



}
