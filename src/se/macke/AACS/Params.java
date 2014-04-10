package se.macke.AACS;

public class Params 
{
	/**
	 * TODO 
	 */
	private final int[][] LAUNCH_BUTTON = 
		{
			{24,25,26,27,28,29},
			{30,31,32,33,34,35},
			{36,37,38,39,40,41},
			{42,43,44,45,46,47},
			{48,49,50,51,52,53},
			{54,55,56,57,58,59},
			{60,61,62,63,64,65}//This row is the scene launch buttons
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
										{94,95,6,97},
										{98,99,100,11},	
										{12,103,104,105}
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


	public Params()
	{

	}

	/**
	 * Finds position of item.
	 * 
	 * @param item - item to be found
	 * @return array of two values - row and column location of item
	 */
	public int[] findPosition(int item)
	{
		int rowCount = 3;
//		int loRow = 0;
		int hiRow = LAUNCH_BUTTON.length-1;

		int colCount = 3;
		int loCol = 0;
		int hiCol = LAUNCH_BUTTON[0].length-1;

		boolean sameRow = false;

//		System.out.println("before loop");
		while(!sameRow)
		{
//			System.out.println("RowCount is: " + rowCount);
			if (item < LAUNCH_BUTTON[rowCount][loCol])
				rowCount = rowCount/2;
			else if (item > LAUNCH_BUTTON[rowCount][hiCol])
				rowCount+=1+(hiRow-rowCount)/2;
			else
				sameRow = true;
		}

		while(!(LAUNCH_BUTTON[rowCount][colCount] == item))
		{
			if (item > LAUNCH_BUTTON[rowCount][colCount])
				colCount += 1+(hiCol-colCount)/2;
			else if (item < LAUNCH_BUTTON[rowCount][colCount])
				colCount = (colCount/2);
			
		}

		int[] position = {rowCount,colCount};

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

}
