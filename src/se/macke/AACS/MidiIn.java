package se.macke.AACS;

import ioio.javax.sound.midi.ShortMessage;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;







import android.util.Log;

public class MidiIn extends Thread
{
	private InputStream inputStream;

	BufferedReader _br;
	/**
	 * Channels used for MIDI input
	 */
	private Uart midi_in_;

	private IOIO _ioio;

	private AACSmain _main;

	//	private MIDIOut _out;

	private final int INPUT_PIN = 6;

	private final int BAUD_RATE = 31250;

	private boolean _running;

	private final String DEBUG_TAG = "MIDI IN: ";

	public MidiIn(IOIO ioio_, AACSmain m_, DigitalOutput led_) throws ConnectionLostException, InterruptedException
	{

		Log.i(DEBUG_TAG  ,"Constructor");

		_ioio = ioio_;
		_main = m_;

		
		midi_in_ = _ioio.openUart(INPUT_PIN, IOIO.INVALID_PIN, BAUD_RATE, 
				Uart.Parity.NONE, Uart.StopBits.ONE);

		led_.write(false);
		Thread.sleep(1);
		led_.write(true);

		inputStream = midi_in_.getInputStream();
		
		_br = new BufferedReader(new InputStreamReader(inputStream));

		_running  = true;


		Log.i(DEBUG_TAG  ,"Constructor finished");
	}




	@Override
	public void run()
	{
		while(_running)
		{
			Log.i(DEBUG_TAG  ,"Looping MIDI in");
			try
			{
				Thread.sleep(2);

			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			/**
			 * An array of bytes read from the input
			 */
			byte[] recievedData = new byte[3];

			try
			{
				Log.i(DEBUG_TAG  ,"Attempting read.. ");

				for (int i = 0 ; i < recievedData.length; i++)
				{
					recievedData[i] = _br.readLine().getBytes()[0];
					Log.i(DEBUG_TAG, String.format("readLine: %02x", recievedData[i]));
				}

				int byteZero = recievedData[0]  & 0xFF;
				int noteNumber = (int) (recievedData[1] & 0xFF);	//Sets note number to int
				int byte2 = recievedData[2] & 0xFF;
				
				Log.i(DEBUG_TAG, "Databyte0: " + byteZero + " data1: " + 
						recievedData[1] + " data2: " + byte2);

				if (recievedData[0] == (ShortMessage.NOTE_ON & 0xFF)) //If it was Note On, set color of pad
					_main.setColorOfPad(noteNumber);
				
			} 
			catch (IOException e)
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

}
