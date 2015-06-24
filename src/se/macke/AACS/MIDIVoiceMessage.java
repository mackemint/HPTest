package se.macke.AACS;

import java.util.LinkedList;

import android.util.Log;
import ioio.javax.sound.midi.InvalidMidiDataException;
import ioio.javax.sound.midi.ShortMessage;

public class MIDIVoiceMessage 
{
	private static final String VOICE_MESSAGE = "VoiceMessage ";

	private final int NOTE_MESSAGE_LIMIT = 3;

	private LinkedList<Integer> _byteList = new LinkedList<Integer>();

	private MidiIn _midiIn;


	public MIDIVoiceMessage(MidiIn midiIn) 
	{
		_midiIn = midiIn;
	}

	/**
	 * Add new message to the voice message buffer
	 * 
	 * @param midiByte the byte buffered
	 * @throws InvalidMidiDataException
	 */
	public void addMIDIByte(int midiByte)  
	{
		_byteList.add(midiByte);
				Log.i(VOICE_MESSAGE, "recieved message" + midiByte);

		if (reachedBufferSizeLimit())
			makeMessage();
	
	}

	/**
	 * @return true if the buffer is filled up
	 */
	private boolean reachedBufferSizeLimit()
	{
		return _byteList.size() == NOTE_MESSAGE_LIMIT;
	}


	/**
	 * Constructs a ShortMessage of the incoming buffer
	 * @throws InvalidMidiDataException
	 */
	public void makeMessage()  
	{
		ShortMessage shortMessage = null;
		try 
		{

			shortMessage = new ShortMessage(_byteList.getFirst(), _byteList.get(1), _byteList.getLast());
			_byteList.removeLast();
			_byteList.removeLast();	//Removing the two last bytes and saving the Status Byte for re-use
		} 
		catch (InvalidMidiDataException e) 
		{
			//			System.out.println("error Invalid midi in makemessage, clearing list");
			e.printStackTrace();
			_byteList.clear();
			return;

		}

		handleNoteMessage(shortMessage);
	}

	private void handleNoteMessage(ShortMessage shortMessage) 
	{

		switch(shortMessage.getCommand())
		{
		case ShortMessage.NOTE_ON:

			_midiIn.setPadColor(shortMessage.getData1(),shortMessage.getData2());
			System.out.println("Note "+shortMessage.getData1() + " on channel " + shortMessage.getChannel() + 
					" is pressed with velocity " + shortMessage.getData2());

			break;

		}

	}

	public void setRunningStatus(boolean b) 
	{
		_byteList.clear();
		
	}

}
