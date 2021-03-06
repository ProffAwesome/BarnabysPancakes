import java.io.*;


public class Npc {
	private String name;	//Name of the book (20)
	private int model, health, damage, range;
	private float rate, speed;
	private int[][] drops;
	//public int recLen = 64;	//Number of bytes in the record
	
	public Npc(String name, int model, int health, int damage, int range, float rate, float speed){
		this.name = name;		//Weapon name (e.g. sword, axe, bow etc.)
		this.model = model;
		this.health = health;
		this.range = range;		//Distance it can fire from
		this.damage = damage;	//Max damage weapon can do
		this.rate = rate;		//Hits per second
		this.speed = speed;
		drops = new int[30][2];	//drops[drop id][info]	info: 0=record number, 1=rarity
	}
	
	public Npc(){
		this.name = "";		//Weapon name (e.g. sword, axe, bow etc.)
		this.model = 0;
		this.health = 0;
		this.range = 0;		//Distance it can fire from
		this.damage = 0;	//Max damage weapon can do
		this.rate = 0;		//Hits per second
		this.speed = 0;
		drops = new int[30][2];
	}
	
	public String getName(){ return this.name; }
	public int getModel(){ return this.model; }
	public int getHealth(){ return this.health; }
	public int getDamage(){ return this.damage; }
	public int getRange(){ return this.range; }
	public float getRate(){ return this.rate; }
	public float getSpeed(){ return this.speed; }
	public int[] getDrop(int index){ return this.drops[index]; }
	public int[][] getDrops(){	return this.drops; }
	
	public void setName(String name){ this.name = name; }
	public void setModel(int model){ this.model = model; }
	public void setHealth(int health){ this.health = health; }
	public void setDamage(int damage){ this.damage = damage; }
	public void setRange(int range){ this.range = range; }
	public void setRate(float rate){ this.rate = rate; }
	public void setSpeed(float speed){ this.speed = speed; }
	public void setDrops(){
		int max = Keyin.inInt("How many drops would you like to enter?");
		this.drops = new int[max][2];
		for (int i = 0; i < max; i++){
			this.drops[i][0] = Keyin.inInt("Enter a drop: (" + i + ")");
			this.drops[i][1] = Keyin.inInt("Enter a rarity: (" + i + ")");
		}
	}
	
	public void printDrops(){
		System.out.println("Drops: (" + this.drops.length + ")");
		System.out.println("ID:\t\tRarity:");
		for (int i = 0; i < this.drops.length; i++)
			System.out.println(drops[i][0] + "\t\t" + drops[i][1]);
	}
	
	
	public void writeRec(){
		if (this.getName() != ""){
    		System.out.println("Name: " + this.getName());
    		System.out.println("Model #: " + this.getModel());
    		System.out.println("Damage: " + this.getDamage());
    		System.out.println("Rate: " + this.getRate() + "/sec");
    		System.out.println("Range: " + this.getRange());
    		System.out.println("Speed: " + this.getSpeed());
    		this.printDrops();
    		System.out.println("--------------------------------------");
		}	//end if
    }
    
    public void readRaf(RandomAccessFile raf) throws IOException{
    	if (raf.length() != 0){
    		raf.seek(0);
	    	this.name = readString(raf);
	    	this.model = raf.readInt();
	    	this.health = raf.readInt();
	    	this.range = raf.readInt();
	    	this.damage = raf.readInt();
	    	this.rate = raf.readFloat();
	    	this.speed = raf.readFloat();
	    	int i = (int) (((raf.length() - raf.getFilePointer())/4)/2);
	    	this.drops = new int[i][2];
	    	for (int j = 0; j < i; j++){
	    		this.drops[j][0] = raf.readInt();
	    		this.drops[j][1] = raf.readInt();
	    	}
    	}
    }
    
    public void writeRaf(RandomAccessFile raf) throws IOException{
    	try{
    		raf.seek(0);
	    	writeString(raf, this.name);
	    	raf.writeInt(this.model);
	    	raf.writeInt(this.health);
	    	raf.writeInt(this.range);
	    	raf.writeInt(this.damage);
	    	raf.writeFloat(this.rate);
	    	raf.writeFloat(this.speed);
	    	for (int i = 0; i < drops.length; i++){
	    		raf.writeInt(drops[i][0]);
	    		raf.writeInt(drops[i][1]);
	    	}
    	}catch(Exception e){	e.printStackTrace();	}
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
