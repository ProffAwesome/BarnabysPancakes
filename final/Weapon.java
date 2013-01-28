import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;


public class Weapon {
	public boolean longRange; //Long range or not
	public boolean equipped;
	public boolean draw;	//Whether or not the mouse is hovering over it
	public int slot;	//Inventory stuff
	public int x, y; 	//Position on map, -1,-1 if not on map)
	public int mid;		//Model # (1.png etc)
	public String name;
	public int damage, dur, rate, range;	//dur: # of swings before it breaks; rate: # of shots per 5 seconds
	private long lastUsed;
	public Image modelsml, modellrg;
	private int recLen = 64;
	
	public Weapon(RandomAccessFile raf, int x, int y){
		
		/*if ((int)(Math.random()*3+1) == 1){
			this.longRange = true;
			try {
				model = ImageIO.read(new File("weapons/1.png"));
			} catch (IOException e) {	System.out.println("Weapon error: #501 \n" + e);	}
		}else{
			this.longRange = false;
			try {
				model = ImageIO.read(new File("weapons/2.png"));
			} catch (IOException e) {	System.out.println("Weapon error: #501 \n" + e);	}
		}
		damage = (int)(Math.random()*20+3);
		dur = (int)(Math.random()*20+3);
		rate = (int)(Math.random()*10+2);*/
		try{
			if ((int)(Math.random()*1+1) == 1){
				this.x = x;
				this.y = y;
				this.range = -1;
				this.slot = 0;
				this.draw = false;
				int numRecs = (int)(raf.length()/recLen);
				int recAt = (int)(Math.random()*(numRecs)-1);
				this.readRaf(raf, recAt);
				Entity.wIndex++;
			}
		}catch(Exception e){	e.printStackTrace(); } //System.out.println("Weapon loading failed\n" + e);	}
		
		
	}
	
	public void readRaf(RandomAccessFile raf, int recordNumber) throws IOException{
    	if (raf.length() >= (recordNumber * recLen)){
    		raf.seek(recordNumber * recLen);
	    	this.name = readString(raf);
	    	this.mid = raf.readInt();
	    	int minDam = raf.readInt();
	    	int maxDam = raf.readInt();
	    	this.damage = (int)((Math.random()*(maxDam-minDam))+minDam);
	    	this.rate = raf.readInt();
	    	this.range = raf.readInt();
	    	this.dur = raf.readInt();
	    //	System.out.println(minDam + "-" + maxDam);
	    	//this.dur = raf.readInt();
	    	//this.mid = raf.readInt();
	    	modelsml = ImageIO.read(new File("items/" + this.mid + "s.png"));
	    	modellrg = ImageIO.read(new File("items/" + this.mid + "l.png"));
    	}
    //	System.out.println(this.name);
    }
	
	public void equip(){
		this.x = -1;
		this.y = -1;
		this.equipped = true;
	}
	
	public void drop(int x, int y){
		if (this.equipped){
			this.x = x;
			this.y = y;
			this.equipped = false;
		}
	}
	
	public void drawWeapon(Graphics g, Display m, int x, int y, boolean small){
		if (small)
			g.drawImage(this.modelsml, x, y, x+25, y+25, 0, 0, 25, 25, m);
		else
			g.drawImage(this.modellrg, x, y, x+25, y+25, 0, 0, 25, 25, m);
	}
	
	public int getDamage(){	return this.damage;	}
	public int getDur(){	return this.dur;	}
	public int getRate(){	return this.rate;	}
	
	public void takeDur(){	this.dur--;	}
	
	private String readString(RandomAccessFile raf) throws IOException{
    	String s = "";
    	for (int i = 0; i < 20; i++)
    		s = s + raf.readChar();
    	return s.trim();
    }
}
