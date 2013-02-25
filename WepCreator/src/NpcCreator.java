/**
 * @(#)Assign2.java
 *
 * Assign2 application
 *
 * @author 
 * @version 1.00 2012/9/13
 */
 import java.io.*;
public class NpcCreator {
	
	public static RandomAccessFile raf;
	
	/**********************************************
	 *Searches for data with the b array by author
	 *b: 		array of books
	 *dataLen:	Number of non-null books contained in b
	 **********************************************/
	public static void searchName(Weapon[] w, int dataLen){
		char cont = 'y';	//Whether the user wants to search again or not
		int found = 0;	//Whether the array set has been found or not
		do{
			String authfName = Keyin.inString("Enter weapon name:");	//The author being searched for
			System.out.println(dataLen);
				for (int i = 0; i < dataLen; i++){	//checks each set of the array
					System.out.println(authfName.toLowerCase() + "==" + w[i].getName().toLowerCase());
					if (authfName.toLowerCase() == w[i].getName().toLowerCase()){	//For some reason... this wasn't working
						System.out.println("Found at array id: " + i);
						found++;
					}	//End if
				}	//End for
			if (found == 0)
				System.out.println("The weapon " + authfName + " was not found");
			else
				System.out.println(found + " matches found.");
			cont = Keyin.inChar("Would you like to search again? (y/n)");
		}while (cont == 'y' || cont == 'Y');	//End do
	}
	
	/**********************************************
	 *Writes the information within the b array to a raf file
	 *b: 		array of books
	 *full:		Number of non-null books contained in b
	 *
	 *returns:	The number of non-null sets in the b array
	 **********************************************/
	public static int interWrite(Weapon[] w, int full){
		System.out.println("\nWriting...");
    	int datLen = 0;	//Returns number of sets in the array
    	try{
    		for (int i = 0; i < full; i++){
    			if (w[i] != null){
    				w[i].writeRaf(raf, i);	//Writes to the raf
    				datLen = i;	//Sets the return value
	    		}	//End if
    		}	//End for
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    	System.out.println("Done!\n");
    	return datLen+1;
	}
	
	/**********************************************
	 *Prints out the information contained in b
	 *b: 		array of books
	 *dataLen:	Number of non-null books contained in b
	 **********************************************/
	public static void interPrint(Weapon[] w, int dataLen){
		System.out.println("There is/are " + dataLen + " weapon available");
		for (int i = 0; i < dataLen; i++){	//writeRec for each set in the array
			w[i].writeRec();
		}	//End for
	}
	
	/**********************************************
	 *Adds a set of a data to the bin
	 *b: 		array of books
	 *dataLen:	Number of non-null books contained in b
	 **********************************************/
	public static void interAdd(Weapon[] w, int dataLen){
		do{
			try {
				w[dataLen].addRaf(raf, dataLen);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}while (Keyin.inChar("Are you sure you want to add this? (y/n)") != 'y');	//Verification
		System.out.println("Successfully added the following book:\n--------------------------------------");
		w[dataLen].writeRec();	//Writes out the new book for the user to see
	}
	
	/**********************************************
	 *Deletes a set of data from the raf file
	 *b: 		array of books
	 *raf:		The raf file used to contain the data
	 **********************************************/
	public static void interDel(Weapon[] w, RandomAccessFile raf){
		try{
			int setNum = Keyin.inInt("Which weapon would you like to delete (Number in the array)?");
			w[setNum].readRaf(raf, setNum);	//Ensures the user wants to delete the book they selected
			w[setNum].writeRec();
			char cont = Keyin.inChar("Are you sure you want to delete this weapon?");
			if (cont == 'y' || cont == 'Y'){
				w[setNum] = new Weapon();	//Replaces the book with a null set
				w[setNum].writeRaf(raf, setNum);	//Writes it out to the raf file selected at the beginning
				System.out.println("Successfully deleted weapon #" + setNum);	//Verification
			}	//End if
		} catch (Exception e) {
			e.printStackTrace();
		}	//readRaf throws errors
	}
	
	/**********************************************
	 *Edits one of the books
	 *b: 		array of books
	 *raf:		The raf file used to contain the data
	 **********************************************/
	public static void interEdit(Weapon[] w, RandomAccessFile raf){
		char cont = 'n';
		int setNum = 0;
		do{	//Gives the user chances to correct typos
			setNum = Keyin.inInt("Which weapon would you like to edit?");
			w[setNum].writeRec();
			cont = Keyin.inChar("Are you sure you want to edit this book? (y = yes, n = try again, e = exit)");
		} while (cont != 'y' && cont != 'Y' && cont != 'e' && cont != 'E');
		if (cont == 'y' || cont == 'Y'){
			try{	//sets all the data in the book to new information
				w[setNum].setName(Keyin.inString("Weapon name: "));
				w[setNum].setModel(Keyin.inInt("Model: "));
				w[setNum].setMinDam(Keyin.inInt("Min damage: "));
				w[setNum].setMaxDam(Keyin.inInt("Max damage: "));
				w[setNum].setRate(Keyin.inInt("Rate: "));
				w[setNum].setRange(Keyin.inInt("Range: "));
				w[setNum].setDur(Keyin.inInt("Max durability: "));
				w[setNum].setRarity(Keyin.inInt("Rarity:"));
			   	w[setNum].writeRaf(raf, setNum);	//Writes the new data to the .bin file selected earlier
			}catch(Exception e){	//writeRaf throws an error
				e.printStackTrace();
			}
		}
	}
	
	/**********************************************
	 *Sorts the information from the raf file and stores it in the b array
	 *b: 		array of books
	 *dataLen:	Number of non-null books contained in b
	 *raf:		The raf file that stores the data
	 **********************************************/
	public static void readRaf(Weapon[] w, RandomAccessFile raf, int dataLen){
		try{
			for (int i = 0; i < dataLen; i++){	//For each set of data in the array
				w[i] = new Weapon();
				w[i].readRaf(raf, i);	//Reads info from a .bin file
			}
		}catch(Exception e){	//readRaf throws an error
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args) {
    	Weapon[] w = new Weapon[20];
    	try{	//sets up the array of books (max. 20)
    		int dataLen = 0;	//amount of not null sets in the array
    		String binName = Keyin.inString("Enter a binary filename to use:");	//Binary file being used throughout the project
	    	raf = new RandomAccessFile(binName, "rw");	//Creates the raf file
    		dataLen = (int)(raf.length()/180);	//Number of data sets in the raf file
    		if (dataLen != 0)
    			dataLen += 1;
    		readRaf(w, raf, dataLen);	//Reads the data sets into the b array
    		char exit = 'h';
    		while (exit != 'e'){	//User interface
    			if(exit == 'h')		//Shows the user commands
    				System.out.println("*****Help:*****\nWrite: w\nEdit: t\nPrint: p\nAdd: a\nSearch: s\nDelete: d\nExit: e\n***************");
    			else if (exit == 'w')	
    				dataLen = interWrite(w, dataLen);	//Writes the array in binary form
	    		else if (exit == 'p')
    				interPrint(w, dataLen);	//Prints the data out to the screen
    			else if (exit == 'a'){	//Adds a set to the .bin array
    				w[dataLen] = new Weapon();
    				w[dataLen].addRaf(raf, dataLen);
    				dataLen++;
    			}
	    		else if (exit == 's')	//Searches for data in the array
    				searchName(w, dataLen);
    			else if (exit == 'd')	//Deletes from the array and writes it to the data file
    				interDel(w, raf);
    			else if (exit == 't')	//Edits the information contained in the bin file
    				interEdit(w, raf);
	    		exit = Keyin.inChar("What would you like to do next? (h for help) ");	//User menu interface
    		}
    	}catch (Exception e){	//Incase something goes wrong in the main()
    		e.printStackTrace();
    	}
    }
}
