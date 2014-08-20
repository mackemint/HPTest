package se.macke.AACS;

public class Params 
{
	/**
	 * The Params file is a place to store all note values and other parameters
	 * for the controller.
	 * 
	 */
	public Params()
	{

	}


	private final int[][] LAUNCH_BUTTON = 
		{
			{24,25,26,27,28,29,65},
			{30,31,32,33,34,35,64},
			{36,37,38,39,40,41,63},
			{42,43,44,45,46,47,62},
			{48,49,50,51,52,53,61},
			{54,55,56,57,58,59,60},
		};


	/**
	 * Default set of notes transmitted to the output queue
	 */
	private final int[][] BUTTON_MATRIX = 
		{
			{0,1,2,3},
			{4,5,6,7},
			{8,9,10,11},
			{12,13,14,15}
		};

	/**
	 * Solo modifier button pressed returns this matrix
	 */
	private final int[][] SOLO_MATRIX = {
			{70,71,72,73},
			{74,75,6,7},
			{8,9,10,11},
			{12,13,14,15}
	};


	/**
	 * Track on modifier button pressed returns this matrix
	 */
	private final int[][] MUTE_MATRIX = {
			{80,81,82,83},
			{84,85,6,7},
			{8,9,10,11},
			{12,13,14,15}
	};


	/**
	 * Shift button matrix
	 */
	private final int[][] SHIFT_MATRIX = 
		{
			{90,91,92,93},
			{94,95,6,7},
			{8,99,100,111},	
			{112,103,104,105}
		};

	/**
	 * Faders and knobs
	 */
	private final int[][] POT_MATRIX1 = {
			{60,61,62,63,64,65},
			{66,67,68,69,70,71},
			{72,73,74,75,76,77}
	};

	private final int[] POT_MATRIX2 = {80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97};

	private final int[] POT_MATRIX3 = {100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117};

	private final int MIDI_CHANNEL = 15;


	/**
	 * Used for performance modes
	 */

	public final String MODE_DJ = "dj";
	public final String MODE_PERFORM = "perform";
	public final String MODE_NOTE = "note";

	private String _mode = MODE_PERFORM;

	/**
	 * Finds position of item.
	 * 
	 * @param item - item to be found
	 * @return array of two values - row and column location of item
	 */
	//	public int[] findPosition(int item)
	//	{
	//		if (item < LAUNCH_BUTTON[0][0] || item > LAUNCH_BUTTON[5][6])
	//			throw new ArrayIndexOutOfBoundsException();
	//		
	//		
	//		int rowCount = 3;
	//		//		int loRow = 0;
	//		int hiRow = LAUNCH_BUTTON.length-1;
	//
	//		int colCount = 3;
	//		int loCol = 0;
	//		int hiCol = LAUNCH_BUTTON[0].length-1;
	//
	//		boolean sameRow = false;
	//
	//		//		System.out.println("before loop");
	//		while(!sameRow)
	//		{
	//			//			System.out.println("RowCount is: " + rowCount);
	//			if (item < LAUNCH_BUTTON[rowCount][loCol])
	//				rowCount = rowCount/2;
	//			else if (item > LAUNCH_BUTTON[rowCount][hiCol])
	//				rowCount+=1+(hiRow-rowCount)/2;
	//			else
	//				sameRow = true;
	//		}
	//
	//		while(!(LAUNCH_BUTTON[rowCount][colCount] == item))
	//		{
	//			if (item > LAUNCH_BUTTON[rowCount][colCount])
	//				colCount += 1+(hiCol-colCount)/2;
	//			else if (item < LAUNCH_BUTTON[rowCount][colCount])
	//				colCount = (colCount/2);
	//
	//		}
	//
	//		int[] position = {rowCount,colCount};
	//
	//		return position;
	//	}
	//I think I read this in a book somewhere, but it seems like a very elegant solution. Typing from memory, so this may or may not work as-is.

	public int[] findPosition(int elem)
	{
		int rows = LAUNCH_BUTTON.length;
		int cols = LAUNCH_BUTTON[0].length-1;
		int start = 0;
		int end = rows*cols - 1;


		int[] position = {-1,-1};

		/**
		 * TODO
		 * This part of the code is never reached 
		 * There is need for further parsing of the note messages to
		 * allow an incoming stream without status bytes.
		 */
		if(elem > 60)
		{
			System.out.printf("Scene launch button pressed!");
			
			
			int col = LAUNCH_BUTTON[0].length;
			position[1] = col;
			for (int i = 0; i < LAUNCH_BUTTON.length; i++)
			{
				System.out.printf("Searching vertically row:%d, col:%d, item is:%d\n",i,position[1],LAUNCH_BUTTON[i][col]);
				
				if(elem == LAUNCH_BUTTON[i][col])
					position[0] = i;
				
			}
		}
		else
		{
			while(start<= end)
			{
				int mid = start + (end-start)/2;
				int row = mid/cols;
				int col = mid% cols;
				int curElem = LAUNCH_BUTTON[row][col];
				
				System.out.printf("Searching row:%d, col:%d, item is:%d\n",row,col,curElem);
				
				if(curElem == elem)
				{
					position[0] = row;
					position[1] = col;
					System.out.printf("Search complete");
					return position;
					
				}
				if(curElem > elem)
					end = mid - 1;
				else
					start = mid + 1;
			}
		}
		return position;
	}

	public int getHardwareButton(int row, int col)
	{
		return BUTTON_MATRIX[row][col];
	}

	public int getSoloButton(int row, int col)
	{
		return SOLO_MATRIX[row][col];
	}

	public int getShiftButton(int row, int col)
	{
		return SHIFT_MATRIX[row][col];
	}

	public int getMuteButton(int row, int col)
	{
		return MUTE_MATRIX[row][col];
	}

	public int getLaunchButton(int row, int col)
	{
		return LAUNCH_BUTTON[row][col];
	}

	public int getPotValue(int row, int col)
	{
		return POT_MATRIX1[row][col];
	}

	public int getMIDIChannel() {

		return MIDI_CHANNEL;
	}

	public int[][] getSoloValues() {
		// TODO Auto-generated method stub
		return SOLO_MATRIX;
	}

	public int[][] getMuteValues() {
		// TODO Auto-generated method stub
		return MUTE_MATRIX;
	}

	public int[][] getShiftValues() {
		// TODO Auto-generated method stub
		return SHIFT_MATRIX;
	}

	public int[][] getNoteValues() {
		// TODO Auto-generated method stub
		return BUTTON_MATRIX;
	}
	/**
	 *	TODO assert the different modes!
	 */
	public void setMode(String mode) 
	{
		_mode = mode;
	}
	public String getMode()
	{
		return _mode;
	}

	public Boolean getNoteMode()
	{
		return _mode.equalsIgnoreCase(MODE_NOTE);
	}

}
