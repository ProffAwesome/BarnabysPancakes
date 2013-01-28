import java.awt.Color;
import java.awt.Graphics;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;


public class NPC {
	double x,y;
	int dX, dY;
	int x1,y1,x2,y2,x3,y3,x4,y4;
	int width, height;
	int type, health, damage, range, speed, maxhp;	//Varies based on model
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
		this.y2 = y1 + 30;
		
		this.x3 = x1 + 30;			//top right
		this.y3 = y1;
		
		this.x4 = x1 + 30;			//bottom right
		this.y4 = y1 + 30;
	}
	
	//TODO: Read this information from a binary file
	public void genStats(int type, int diff){
		try {
			//RandomAccessFile raf = new RandomAccessFile("npc/" + type + "/stats.bin", "rw");
			switch (type){
				case 10:
				{
					//int nDrops = ((int)raf.length() - recLen)/8;
					//drops = new int[nDrops][2];
					this.health = 20 + 5*diff;
					this.maxhp = this.health;
					this.rate = (float) 1.4;
					this.damage = 5+diff;
					this.range = 1;
					this.speed = 2;
					break;
				}
			}
			//raf.close();
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
		//	RandomAccessFile raf = new RandomAccessFile("items/weps.bin", "rw");
			RandomAccessFile raf = new RandomAccessFile("npc/" + this.type + "/weps.bin", "rw");
			Entity.w[Entity.wIndex] = new Weapon(raf, (int)this.x, (int)this.y);
		} catch (Exception e) {	System.out.println("Broken: " + e);	}
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
}
