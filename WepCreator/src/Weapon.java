import java.io.*;


public class Weapon {
	private String name;	//Name of the book (20)
	private int minDam, maxDam, dur, range, model, rarity;
	private float rate;
	public int recLen = 68;	//Number of bytes in the record
	
	public Weapon(String name, int model, int minDam, int maxDam, int dur, float rate, int range, int rarity){
		this.name = name;		//Weapon name (e.g. sword, axe, bow etc.)
		this.model = model;
		this.minDam = minDam;	//Minimum damage weapon can do
		this.maxDam = maxDam;	//Max damage weapon can do
		this.rate = rate;		//Hits per second
		this.range = range;		//Distance it can fire from
		this.dur = dur;			//Number of hits till it breaks (optional)
		this.rarity = rarity;
	}
	
	public Weapon(){
		this.name = "";
		this.model = 0;
		this.minDam = 0;
		this.maxDam = 0;
		this.rate = 0;
		this.range = 0;
		this.dur = 0;
		this.rarity = 0;
	}
	
	public String getName(){ return name; }
	public int getMinDam(){ return minDam; }
	public int getMaxDam(){ return maxDam; }
	public float getRate(){ return rate; }
	public int getRange(){ return range; }
	public int getDur(){ return dur; }
	public int getModel(){ return model; }
	public int getRarity(){ return rarity; }
	
	public void setName(String name){ this.name = name; }
	public void setMinDam(int minDam){ this.minDam = minDam; }
	public void setMaxDam(int maxDam){ this.maxDam = maxDam; }
	public void setRate(float rate){ this.rate = rate; }
	public void setRange(int range){ this.range = range; }
	public void setDur(int dur){ this.dur = dur; }
	public void setModel(int model){ this.model = model; }
	public void setRarity(int rarity){ this.rarity = rarity; }
	
	public void writeRec(){
		if (this.getName() != ""){
    		System.out.println("Name: " + this.getName());
    		System.out.println("Model #: " + this.getModel());
    		System.out.println("Damage: " + this.getMinDam() + "-" + this.getMaxDam());
    		System.out.println("Rate: " + this.getRate() + "/sec");
    		System.out.println("Range: " + this.getRange());
    		System.out.println("Durability: " + this.getDur());
    		System.out.println("Rarity: " + this.getRarity() + "%");
    		System.out.println("--------------------------------------");
		}	//end if
    }
    
    public void readRaf(RandomAccessFile raf, int recordNumber) throws IOException{
    	if (raf.length() != 0 && raf.length() >= (recordNumber * recLen)){
    		raf.seek(recordNumber * recLen);
	    	this.name = readString(raf);
	    	this.model = raf.readInt();
	    	this.minDam = raf.readInt();
	    	this.maxDam = raf.readInt();
	    	this.rate = raf.readFloat();
	    	this.range = raf.readInt();
	    	this.dur = raf.readInt();
	    	this.rarity = raf.readInt();
    	}
    }
    
    public void writeRaf(RandomAccessFile raf, int recordNumber) throws IOException{
    	try{
	    	if (recordNumber != 0)
	    		raf.seek(recordNumber * recLen);
	    	writeString(raf, this.name);
	    	raf.writeInt(this.model);
	    	raf.writeInt(this.minDam);
	    	raf.writeInt(this.maxDam);
	    	raf.writeFloat(this.rate);
	    	raf.writeInt(this.range);
	    	raf.writeInt(this.dur);
	    	raf.writeInt(this.rarity);
    	}catch(Exception e){	System.out.println("Breaks here (#1!): " + e);	}
    }
    
    public void addRaf(RandomAccessFile raf, int dataLen) throws IOException{
    	raf.seek(raf.length());
    	this.name = Keyin.inString("Weapon name:");
    	this.model = Keyin.inInt("Model #:");
    	this.minDam = Keyin.inInt("Min damage:");
    	this.maxDam = Keyin.inInt("Max damage:");
    	this.rate = Float.parseFloat(Keyin.inString("Rate:"));
    	this.range = Keyin.inInt("Range:");
    	this.dur = Keyin.inInt("Max durability:");
    	this.rarity = Keyin.inInt("Rarity:");
    	/*writeString(raf, this.bookName);
    	writeString(raf, this.fName);
    	writeString(raf, this.lName);
    	writeString(raf, this.publisher);
    	raf.writeLong(this.isbnNum);
    	raf.writeDouble(this.price);
    	raf.writeInt(this.stock);*/
    	this.writeRaf(raf, 0);
}
    
    private void writeString(RandomAccessFile raf, String s) throws IOException{
    	int padLen = 0;
    	if (s.length() > 20)
    		padLen = 0;
    	else
    		padLen = 20 - s.length();
    	for (int i = 0; i < s.length(); i++)
    		raf.writeChar(s.charAt(i));
    	if (padLen > 0)
    		for (int j = 0; j < padLen; j++)
    			raf.writeChar(' ');
    }
    private String readString(RandomAccessFile raf) throws IOException{
    	String s = "";
    	for (int i = 0; i < 20; i++)
    		s = s + raf.readChar();
    	return s.trim();
    }
}
