package se.macke.hptest;

/**
 * A destination is used for adding messages to the output queue
 * @author macke
 *
 */
public class Destination 
{

	private HPMainActivity _main;
	
	private int _cc;

	/**
	 * The value sent to the output queue
	 */
	private int _destinationValue;
	



	
	/**
	 * Creates a new destination
	 * @param cc - the CC message
	 * @param main main activity
	 */
	public Destination(int cc, HPMainActivity main)
	{
		_main = main;
		_cc = cc;

	}

	public Destination()
	{
		_destinationValue = 11;
		System.out.println("Created new destination");
	}

	/**
	 * Used for setting the destination value
	 * @param value
	 */
	public void setValue(int value)
	{
		_destinationValue = value;
		_main.addCcToQueue(_cc, _destinationValue);
		System.out.printf("Destination %d was set to: %d/n",_cc, _destinationValue);
	}

	//	public void setDestination(ProgressBar d)
	//	{
	//		_destination = d;
	//	}

	public float getValue()
	{
		System.out.println("Returned destination value of: " + _destinationValue);
		return _destinationValue;
	}



}
