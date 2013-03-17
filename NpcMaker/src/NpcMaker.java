/**
 * @(#)Assign2.java
 *
 * Assign2 application
 *
 * @author 
 * @version 1.00 2012/9/13
 */
 import java.io.*;
import java.nio.file.Path;
public class NpcMaker {
	public static Npc n = new Npc();
	public static RandomAccessFile raf;
	
	/**********************************************
	 *Writes the information within the b array to a raf file
	 *b: 		array of books
	 *full:		Number of non-null books contained in b
	 *
	 *returns:	The number of non-null sets in the b array
	 **********************************************/
	public static void interWrite(){
		System.out.println("\nWriting...");
    	try{
			if (n != null){
				n.writeRaf(raf);	//Writes to the raf
    		}	//End if
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    	System.out.println("Done!\n");
	}
	
	/**********************************************
	 *Prints out the information contained in b
	 *b: 		array of books
	 *dataLen:	Number of non-null books contained in b
	 **********************************************/
	public static void interPrint(Npc n){
		n.writeRec();
	}
	
	/**********************************************
	 *Edits one of the books
	 *b: 		array of books
	 *raf:		The raf file used to contain the data
	 **********************************************/
	public static void interEdit(RandomAccessFile raf){
		char cont = 'n';
		do{	//Gives the user chances to correct typos
			n.writeRec();
			cont = Keyin.inChar("Are you sure you want to edit this book? (y = yes, n = try again, e = exit)");
		} while (cont != 'y' && cont != 'Y' && cont != 'e' && cont != 'E');
		if (cont == 'y' || cont == 'Y'){
			try{	//sets all the data in the book to new information
				raf.seek(0);
				n.setName(Keyin.inString("NPC Name:"));
				n.setModel(Keyin.inInt("Model:"));
				n.setHealth(Keyin.inInt("Health:"));
				n.setDamage(Keyin.inInt("Damage:"));
				n.setRange(Keyin.inInt("Range:"));
				n.setRate(Float.parseFloat(Keyin.inString("Rate:")));
				n.setSpeed(Float.parseFloat(Keyin.inString("Speed:")));
				n.setDrops();
			   	n.writeRaf(raf);	//Writes the new data to the .bin file selected earlier
			}catch(Exception e){	//writeRaf throws an error
				System.out.println("Invalid input.");
			}
		}
	}
	
    public static void main(String[] args) {
    	try{	//sets up the array of books (max. 20)
    		int dataLen = 0;	//amount of not null sets in the array
    		String binName = Keyin.inString("Enter a binary filename to use:");	//Binary file being used throughout the project
	    	raf = new RandomAccessFile(binName, "rw");	//Creates the raf file
	    	try{
	    		raf.getFilePointer();
	    	}catch (FileNotFoundException e){	System.err.println("YOU MUST CREATE THE FILE \"" + binName + "\" TO CONTINUE!!!");	}
    		dataLen = (int)(raf.length()/180);	//Number of data sets in the raf file
    		if (dataLen != 0)
    			dataLen += 1;
    		n.readRaf(raf);	//Reads the data sets into the b array
    		char exit = 'h';
    		while (exit != 'e'){	//User interface
    			if(exit == 'h')		//Shows the user commands
    				System.out.println("*****Help:*****\nWrite: w\nEdit: t\nPrint: p\nNew File: n\nExit: e\n***************");
    			else if (exit == 'w')	
    				interWrite();	//Writes the array in binary form
	    		else if (exit == 'p')
    				n.writeRec();
    			else if (exit == 't')	//Edits the information contained in the bin file
    				interEdit(raf);
    			else if (exit == 'n'){
    				String bName = Keyin.inString("Enter a binary filename to use:");	//Binary file being used throughout the project
    				raf = new RandomAccessFile(bName, "rw");	//Creates the raf file
    			}
	    		exit = Keyin.inChar("What would you like to do next? (h for help) ");	//User menu interface
    		}
    	}catch (Exception e){	//Incase something goes wrong in the main()
    		e.printStackTrace();
    	}
    }
}
