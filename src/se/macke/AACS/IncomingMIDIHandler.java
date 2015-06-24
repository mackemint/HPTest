package se.macke.AACS;

import android.util.Log;
import ioio.javax.sound.midi.ShortMessage;
import ioio.lib.api.IOIO;

public class IncomingMIDIHandler 
{

	private static final String DEBUG_TAG = "Handler";

	MIDIBeatClock _midiBeatClock;

	MIDIVoiceMessage _midiVoiceMessage;

	/**
	 * Take this from the Params class TODO
	 */
	private final int MIDI_CHANNEL = 15;

	private boolean _runningStatus = false;

//	private final int BYTE_LIMIT = 3;
//
//	private int _byteCount = 0;
//
//	private MidiIn _midiIn;

	public IncomingMIDIHandler(MIDIBeatClock midiBeatClock, MidiIn midiIn)
	{
		_midiBeatClock = midiBeatClock;	
		_midiVoiceMessage = new MIDIVoiceMessage(midiIn);
		
//		_midiIn = midiIn;

	}

	public void add(int midiByte)  
	{

		Log.i(">>Handler: ", "adding message " + midiByte);
//		Log.i(DEBUG_TAG, String.format("reading: %02x", midiByte));

		/**
		MIDI running status ....
		If a voicemessage is recieved, we exit running status.
		If this message happens to be a a Note On, running status is  set to true.
		*/
		
		if(midiByte > 127 && midiByte != 0x9F && midiByte < 0xF8)
			setRunningStatus(false);
		
		
		switch (midiByte)
		{
		case ShortMessage.NOTE_ON + MIDI_CHANNEL:

			Log.i("Handler: ","New Note message, engaging Running Status");
			_runningStatus = true;

			break;
		case ShortMessage.CONTROL_CHANGE + MIDI_CHANNEL:
			
			break;

		case ShortMessage.START:
		case ShortMessage.SONG_POSITION_POINTER:
			_midiBeatClock.setRunningStatus(true);
			return;
			
		case ShortMessage.STOP:
			_midiBeatClock.setRunningStatus(false);
			return;
		case ShortMessage.TIMING_CLOCK:
			_midiBeatClock.tick();	
			return;
		case ShortMessage.ACTIVE_SENSING:
			return;
		}

		if(_runningStatus)
		{
			Log.i("Handler: ", "adding voiceMessage" + midiByte);

			_midiVoiceMessage.addMIDIByte(midiByte);

		}
	}
	
	
	

	private void setRunningStatus(boolean b) 
	{
		Log.i("adding ","Clearing Running Status!");
		
		_midiVoiceMessage.setRunningStatus(_runningStatus = b);
	}


}





