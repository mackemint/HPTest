package se.macke.hptest;

import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class RowListener implements OnSeekBarChangeListener
{
	/**
	 * Handler from main activity
	 */
	private InputHandler _inputHandler;

	/**
	 * The top row of seek bars
	 */
	private Destination[] _destination;

	/**
	 * Performance pads from main activity
	 */

	//	private Button[][] _performancePad;

	/**
	 * Used for setting textview status
	 */
	private HPMainActivity _main;

	private final int MIN = 0;
	private final int MAX = 2;

	/**
	 * Creates a RowListener with knowledge of the main activity
	 * Used for the top seek row
	 * @param inputHandler
	 * @param _destination
	 * @param _performancePad 
	 * @param main
	 */
	public RowListener(InputHandler inputHandler, Destination[] destination, HPMainActivity main) 
	{
		_main = main;
		_destination = destination;
		_inputHandler = inputHandler;
		//		_performancePad = performancePad;
	}

	/**
	 * Creates a RowListener that only has an inputhandler as a peer object.
	 * Used for the bottom row.
	 * 
	 * @param inputHandler
	 */
	public RowListener(InputHandler inputHandler) 
	{ 
		_inputHandler = inputHandler;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) 
	{
		try 
		{

			for (int i = 0 ; i <= MAX; i++)
			{
//				if(seekBar == _destination[i])
				{
					progress += 12;
//					System.out.println("id: " + _destination[i].getId());

//					if(seekBar.getProgress() > 0)	//Doesn't set the color if it is unchanged
//						setButtonColor(i, progress);
					setViewText(i, progress);
				}
			}
		}

		catch(NullPointerException e)
		{
			Log.i("NPE","Bottom row does not have knowledge.");
		}
		//Sets takeover mode to true and takes control of destination.
		//
//		if(seekBar.getId() == R.id.seekBar03 || seekBar.getId() == R.id.seekBar04 || seekBar.getId() == R.id.seekBar05)
		{
			_inputHandler.setSourceValue(progress);
			_inputHandler.valueListener();

		}
	}


	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		//Returns from takeover mode and releases control
		//
//		if(seekBar.getId() == R.id.seekBar00 || seekBar.getId() == R.id.seekBar01 || seekBar.getId() == R.id.seekBar02)
		{
			_inputHandler.setTakeOver(false);
//			_inputHandler.getDestination().setTakeover(false);

		}

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) 
	{
		// TODO Auto-generated method stub

	}

//	private void setButtonColor(int i, int progress) {
//		_main.setButtonColor(i, progress);
//	}

	private void setViewText(int row, int progress)
	{
		//		System.out.println("textviewtext col is: " + col);
//		_main.setTextViewText(row, progress);
	}

}
