package se.macke.AACS;

public class SettingBrowser 
{
	int[] _counter = new int[6];
	
	private final int LIMIT = 8;
	
	/**
	 * Counts up the value of position
	 * Wraps around if it reaches the limit
	 * 
	 * @param pos - the position to be increased
	 * @return value of pos + 1
	 */
	int countUpPosition(int pos)
	{
		return isLimitReached(pos,_counter[pos]++);	
	}


	private int isLimitReached(int pos,int i) 
	{
		if(i > LIMIT)
			return _counter[pos] = 0;
		else
			return i;
	}

}
