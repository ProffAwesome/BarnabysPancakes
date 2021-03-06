import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import java.io.RandomAccessFile;


public class NPC {
	double x,y;
	int dX, dY;
	int x1,y1,x2,y2,x3,y3,x4,y4;
	int width, height;
	int type, health, damage, range, maxhp;	//Varies based on model
	float speed;
	float rate;
	int index;		//Increases throughout
	NPC next;
	NPC prev;
	long update = 0;
	long hit = 0;
	long attack = 0;
	int recLen = 20;
	int[][] drops;
	
	public NPC(int index, int type, int diff, double x, double y){
		this.index = index;
		this.type = type;
		this.x = x;
		this.y = y;
		this.genStats(type, diff);
		
		this.x1 = (int)this.x;	//top left
		this.y1 = (int)this.y;
		
		this.x2 = x1;				//bottom left
		this.y2 = y1 + this.height;
		
		this.x3 = x1 + this.width;			//top right
		this.y3 = y1;
		
		this.x4 = x1 + this.width;			//bottom right
		this.y4 = y1 + this.height;
	}
	
	//TODO: Read this information from a binary file
	public void genStats(int type, int diff){
		try {
			RandomAccessFile raf = new RandomAccessFile("npc/" + type + "/stats.bin", "rw");
			if (raf.length() != 0){
	    		raf.seek(0);
	    		@SuppressWarnings("unused")
				String tName = readString(raf);
		    	@SuppressWarnings("unused")
				int tModel = raf.readInt();
		    	this.health = raf.readInt();
		    	this.maxhp = this.health;
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
			raf.close();
		} catch (Exception e) {	e.printStackTrace();	}
	}
	
	public void resetHitBox(){
		this.x1 = (int)this.x;	//top left
		this.y1 = (int)this.y;
		
		this.x2 = this.x1;				//bottom left
		this.y2 = this.y1 + this.width;
		
		this.x3 = this.x1 + this.height;			//top right
		this.y3 = this.y1;
		
		this.x4 = this.x1 + this.height;			//bottom right
		this.y4 = this.y1 + this.width;
	}
	
	public void resetCoords(){
		this.x = this.x1;
		this.y = this.y1;
		resetHitBox();
	}
	
	public void showHealth(Graphics g){
		g.setColor(Color.RED);
		g.fillRect(dX, dY-10, x3-x1, 8);
		g.setColor(Color.GREEN);
		int wid = (int)((this.health*(x3-x1))/this.maxhp);
		g.fillRect(dX, dY-10, wid, 8);
	}
	
	public void hitPlayer(){
		if (this.range <= 2){
			if (System.currentTimeMillis() - this.attack > this.rate*1000){
				Display.p.takeHealth(this.damage);
				this.attack = System.currentTimeMillis();
			}
		}
	}
	
	public void killNpc(){
		try {
			boolean found = false;
			int num = (int)(Math.random()*100+1);
			int full = 0;
			for (int i = 0; i < drops.length && !found; i++){
				if (num < (full+drops[i][1])){
					found = true;
					Entity.w[Entity.wIndex] = new Weapon(drops[i][0]);
					Entity.w[Entity.wIndex].x = (int)this.x;
					Entity.w[Entity.wIndex].y = (int)this.y;
					Entity.wIndex++;
				}else
					full += drops[i][1];
			}
			if (!found)
				System.out.println(num + " No weapon found;  drop a random item");
			RandomAccessFile raf = new RandomAccessFile("items/weps.bin", "rw");
		} catch (Exception e) {	e.printStackTrace();	}
		//Entity.wIndex++;
		//System.out.println(Entity.wIndex);
		if (this.next == null){	//Last npc in series
			if (Entity.root != this){	// Not the root npc
				int i = this.index-1;
				NPC prev = null;
				boolean found = false;
				while (!found){	//Finds the npc that links to the dead one
					prev = Entity.getEne(Entity.root, i);
					if (prev != null)
						if (prev.next == this)
							found = true;
					i--;
				}
				if (prev != null)
					prev.next = this.next;
			}else
				Entity.root = null;
		}else{	//More npcs
			if (Entity.root != this){	// Not the root npc
				int i = this.index-1;
				NPC prev = null;
				boolean found = false;
				while (!found){	//Finds the npc that links to the dead one
					prev = Entity.getEne(Entity.root, i);
					if (prev != null && prev.next == this)
						found = true;
					i--;
				}
				if (prev != null)
					prev.next = this.next;
			}else	//Replace the root
				Entity.root = Entity.root.next;
		}
	}
	
	public void takeHealth(int amount){
		this.health -= amount;
		this.hit = System.currentTimeMillis();
		if (this.health <= 0)
			killNpc();
		this.update = System.currentTimeMillis();
	}
	
	private String readString(RandomAccessFile raf) throws IOException{
    	String s = "";
    	for (int i = 0; i < 20; i++)
    		s = s + raf.readChar();
    	return s.trim();
    }
}
