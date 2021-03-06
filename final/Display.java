//-Xmx1G -Xms1G

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.*;
import java.net.URL;
import javax.imageio.*;
import javax.media.jai.*;

//TODO: add stamina for sprinting?

public class Display extends Applet implements MouseListener, MouseMotionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	
	boolean mapImageVariables = false;
	
	public static int w, h; //w is the width of the applet, h is the height
	int x, y;
	int fps = 32;
	int skip = 32;//1000/fps;
	public static int menu = 0; //0 = in-game, 1 = main menu, 2 = options, etc.
	public static boolean running = true; //while game is running
	public static boolean runT = true;
	public static boolean hudon = true, mapon = true, noclip = false, walls3D = true;
	boolean invopen = false;
	boolean invdrag = false;
	Weapon invtemp = new Weapon();
	boolean moveup = false;
	boolean movedown = false;
	boolean moveleft = false;
	boolean moveright = false;
	boolean run = false;
	public static boolean mapChanged = false;
	public static boolean classicControls = false;
	double movex, movey;
	long nextframe;
	MediaTracker tr;
	int mx, my, mapx, mapy;
	double prot, test, mxd, myd, PI;
	boolean pressed;
	Image player, dark, paused, dead, loadingmap, tex, texdark, waterfade, cursors, hudinv, minimapgfx, invmain, weapdisp;
	public static Image scroll;
	Cursor cursor;
	Thread t;
	public static Player p = new Player(true);
	public static Entity e = new Entity(p);
	public static Proj rootProj;
	
	Image dbImage, mapback, minimap, minimapback;
	BufferedImage mapfront;
	Graphics g, g2;
	
	public static int minx, miny, maxx, maxy;
	public static int[][] map;
	public static int maph = 0;
	public static int mapw = 0;
	public static String mapName;
	boolean[][] fade;
	
	long timea = System.currentTimeMillis(), timeb;
	
	/**
	 * this gets rid of exception for not using native acceleration
	 */
	static { System.setProperty("com.sun.media.jai.disableMediaLib", "true"); }
	
	/**
	 * Initialises variables.
	 *
	 * @param	height	height of the displayed area
	 * @param	width	width of the displayed area
	 */
	public final void init(int height, int width) {
		h = height;
		w = width;
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		try { //load all images
			player = ImageIO.read(getClass().getResource("gfx/rotationTest.png"));
			dark = ImageIO.read(getClass().getResource("gfx/darkscreen.png"));
			paused = ImageIO.read(getClass().getResource("gfx/gamepaused.png"));
			dead = ImageIO.read(getClass().getResource("gfx/dead.png"));
			loadingmap = ImageIO.read(getClass().getResource("gfx/loadingmap.png"));
			tex = ImageIO.read(getClass().getResource("gfx/textures.png"));
			texdark = ImageIO.read(getClass().getResource("gfx/texturesdark.png"));
			waterfade = ImageIO.read(getClass().getResource("gfx/waterfade.png"));
			cursors = ImageIO.read(getClass().getResource("gfx/cursor.png")); //change cursor to be current weapon?
			hudinv = ImageIO.read(getClass().getResource("gfx/hudinv.png"));
			minimapgfx = ImageIO.read(getClass().getResource("gfx/minimap.png"));
			invmain = ImageIO.read(getClass().getResource("gfx/inventory.png"));
			weapdisp = ImageIO.read(getClass().getResource("gfx/weapondisplay.png"));
			scroll = ImageIO.read(getClass().getResource("items/scroll.png"));
		}
		catch(Exception e) { e.printStackTrace(); }
		
	//	cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursors, new Point(0, 0), "testcursor");
	//	this.setCursor(cursor);
		
		pressed = false;
		mapx = 0;
		PI = Math.PI;
		movex = 0.0;
		movey = 0.0;
		map = readInMap("", getClass().getResource("maps/BPisland.map"), false, 1);
		
		this.requestFocus();
	} //end init()
	
	public final void mouseEntered(MouseEvent e) {
		requestFocusInWindow();
		draw();
	}
	public final void mouseExited(MouseEvent e) {
		if (running)
			pauseGame();
		e.consume();
	}
	public final void mouseClicked(MouseEvent e) {
		if (invopen && !p.dead) {
			int xstart = w/2-invmain.getWidth(this)/2 + 15;
			int ystart = (h-58)/2-invmain.getHeight(this)/2 + 46;
			if (mx >= xstart && mx < xstart+448 && my >= ystart && my < ystart+224) {
				int invmouse = (mx-xstart)/56 + ((my-ystart)/56)*8;
				if (invdrag) {
					if (p.inv[invmouse].wid == -1) {
						p.inv[invmouse] = invtemp;
						invtemp = null;
						invtemp = new Weapon();
						invdrag = false;
					}
					else {
						Weapon temp = p.inv[invmouse];
						p.inv[invmouse] = invtemp;
						invtemp = temp;
					}
				}
				else if (!invdrag && p.inv[invmouse].wid != -1) {
					invtemp = p.inv[invmouse];
					p.inv[invmouse] = null;
					p.inv[invmouse] = new Weapon();
					invdrag = true;
				}
			}
			else if (mx >= 0 && mx < hudinv.getWidth(this) && my >= h-hudinv.getHeight(this) && my < h) {
				if (mx >= 7 && mx <= 57 && my >= h-hudinv.getHeight(this)+2 && my <= h-hudinv.getHeight(this)+52) { //if q
					if (invdrag) {
						if (p.q.wid == -1) {
							p.q = invtemp;
							invtemp = null;
							invtemp = new Weapon();
							invdrag = false;
						}
						else {
							Weapon temp = p.q;
							p.q = invtemp;
							invtemp = temp;
						}
					}
					else if (!invdrag && p.q.wid >= 0) {
						invtemp = p.q;
						p.q = null;
						p.q = new Weapon();
						invdrag = true;
					}
				}
				else if (mx >= 33 && mx <= 83 && my >= h-hudinv.getHeight(this)+60 && my <= h-hudinv.getHeight(this)+110) { //if l
					if (invdrag) {
						if (p.l.wid == -1) {
							p.l = invtemp;
							invtemp = null;
							invtemp = new Weapon();
							invdrag = false;
						}
						else {
							Weapon temp = p.l;
							p.l = invtemp;
							invtemp = temp;
						}
					}
					else if (!invdrag && p.l.wid >= 0) {
						invtemp = p.l;
						p.l = null;
						p.l = new Weapon();
						invdrag = true;
					}
				}
				else if (mx >= 91 && mx <= 141 && my >= h-hudinv.getHeight(this)+86 && my <= h-hudinv.getHeight(this)+136) { //if r
					if (invdrag) {
						if (p.r.wid == -1) {
							p.r = invtemp;
							invtemp = null;
							invtemp = new Weapon();
							invdrag = false;
						}
						else {
							Weapon temp = p.r;
							p.r = invtemp;
							invtemp = temp;
						}
					}
					else if (!invdrag && p.r.wid >= 0) {
						invtemp = p.r;
						p.r = null;
						p.r = new Weapon();
						invdrag = true;
					}
				}
			}
			e.consume();
			draw();
		}
		else if (p.dead){
			System.out.println("respawn");
			readInMap(mapName, getClass().getResource(""), true, 1);
			p.resetStats();
		}
	}
		//if (e.getClickCount() == 2) { }
	public final void mousePressed(MouseEvent e) {
		if (running) {
			mx = e.getX();
			my = e.getY();
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (p.l.wid == 10)
					p.l = invAction(p.l);
				else
					invAction(p.l);
			}
			else if (e.getButton() == MouseEvent.BUTTON3) {
				if (p.r.wid == 10)
					p.r = invAction(p.r);
				else
					invAction(p.r);
			}
			e.consume();
		}
		else if (!invopen) {
			runT = true;
			draw();
		}
	}
	public final void mouseReleased(MouseEvent e) { }
	public final void mouseMoved(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		if (running) {
			mxd = mx-w/2;
			myd = my-h/2;
			if (mxd == 0)
				prot = myd >= 0? PI/2: -PI/2;
			else {
				prot = Math.atan(myd/mxd);
				if (mxd < 0)
					prot += PI;
			}
			if (prot < 0)
				prot += 2*PI;
			e.consume();
		}
		else if (invopen)
			draw();
	}
	public final void mouseDragged(MouseEvent e) { }
	
	public final void keyPressed(KeyEvent e) {
		if (running) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_W:
					moveup = true;
					break;
				case KeyEvent.VK_S:
					movedown = true;
					break;
				case KeyEvent.VK_A:
					moveleft = true;
					break;
				case KeyEvent.VK_D:
					moveright = true;
					break;
				case KeyEvent.VK_Q:
					if (p.q.wid == 10)
						p.q = invAction(p.q);
					else
						invAction(p.q);
					break;
				case KeyEvent.VK_E:
					invopen = true;
					pauseGame();
					break;
				case KeyEvent.VK_F:
					pickupWeap();
					break;
				case KeyEvent.VK_SHIFT:
					run = true;
					break;
				case KeyEvent.VK_ESCAPE:
					pauseGame();
					break;
			}
		}
		else if (invopen && !invdrag) {
			invopen = false;
			invtemp = null;
			invtemp = new Weapon();
			runT = true;
			draw();
		}
	}
	public final void keyReleased(KeyEvent e) {
		if (running) {
			switch (e.getKeyCode()){
				case KeyEvent.VK_W:
					moveup = false;
					break;
				case KeyEvent.VK_S:
					movedown = false;
					break;
				case KeyEvent.VK_A:
					moveleft = false;
					break;
				case KeyEvent.VK_D:
					moveright = false;
					break;
				case KeyEvent.VK_SHIFT:
					run = false;
					break;
			}
		}
	}
	public final void keyTyped(KeyEvent e) { }
	
	public final void pauseGame() {
		running = false;
		moveup = false;
		movedown = false;
		moveleft = false;
		moveright = false;
		run = false;
	}
	
	public final Weapon invAction(Weapon type) {
		if (type.wid < 5) { //short-range weapons
			if (type.wid == 0) { //open-fist melee
				hitNPC(Entity.root, (int)(p.x+31*Math.cos(prot)), (int)(p.y+31*Math.sin(prot)), type.damage);
		//		System.out.println((int)(p.x+31*Math.cos(prot)) + "," + (int)(p.y+31*Math.sin(prot)));
			}
			else {
				int range = 4;
				hitNPC(Entity.root, (int)(p.x+(range*10+21)*Math.cos(prot)), (int)(p.y+(range*10+21)*Math.sin(prot)), type.damage);
		//		System.out.println((int)(p.x+(range*10+21)*Math.cos(prot)) + "," + (int)(p.y+(range*10+21)*Math.sin(prot)));
			}
		}
		else if (type.wid < 15) {  //long-range weapons
			if (type.wid == 10 && type.ammo > 0) { //gun
				addProj(p.x, p.y, p.x-(w/2-mx), p.y-(h/2-my), 15);
				type.ammo--;
			}
			if (type.ammo == 0)
				System.out.println("No ammo");
		}
		else if (type.wid < 20) { //magic
			if (type.wid == 15 && p.mana >= 20) { //teleportation
				if ((mx-w/2)+p.x > 0 && (mx-w/2)+p.x < mapw*48 && (my-h/2)+p.y > 0 && (my-h/2)+p.y < maph*48) {
					if (!noclip && map[((my-h/2)+(int)p.y)/48][((mx-w/2)+(int)p.x)/48] < 60 && map[((my-h/2)+(int)p.y)/48][((mx-w/2)+(int)p.x)/48] != -1) {
						p.x = (mx-w/2) + p.x;
						p.y = (my-h/2) + p.y;
						playerCollision();
						p.mana -= 20;
					}
					else if (noclip) {
						p.x = (mx-w/2) + p.x;
						p.y = (my-h/2) + p.y;
						p.mana -= 20;
					}
				}
			}
		}
		else if (type.wid > 260) { //items
			;
		}
		return type;
	}
	
	public final void pickupWeap() {
		for (int i = 0; i < Entity.wIndex; i++){
			try{
				if (Entity.w[i].wid != -1){
					int x = (w/2)-(int)(p.x-Entity.w[i].x);
					int y = (h/2)-(int)(p.y-Entity.w[i].y);
					if (mx < x+26 && mx > x && my < y+26 && my > y && mx-w/2 < 100 && my-h/2 < 100) {	//Mousing over weapon
						p.addWeap(Entity.w[i]);
						Entity.w[i] = new Weapon();
					}
				}
			}catch(Exception x){	System.out.println("Here: " + x);	}
		}
	}
	
	public final boolean hitNPC(NPC at, int hitx, int hity, int dam) {
		//try{
			if (at != null){
				if ((hitx < at.x3 && hitx > at.x1) && (hity < at.y2 && hity > at.y1)){
					at.takeHealth(dam);
					return true;
				}else{
					if (at.next != null)
						return hitNPC(at.next, hitx, hity, dam);
					else
						return false;
				}
			}else	//There are no more npcs left
				return false;
		/*}catch(Exception e){	
			System.out.println("Projectile Error: #401\n" + e);
			return false;
		}*/
	}
	
	public static final int[][] readInMap(String fnStr, URL fnURL, boolean isString, int spawnNum) { //spawnNum default is 1
		//resets
		int[][] area = null;
		e = new Entity(p);
		rootProj = null;
		
		if (!isString) { //various formatting fixes
			String temp = fnURL.toString().substring(6);
			temp = temp.replaceAll("%20", " ");
			temp = temp.replaceAll("%5c", "/");
			fnStr = temp;
		}
		mapName = fnStr;
		
		try{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fnStr)));
			int entitynum = 0;
			int ln = 0;
			int teleports = 0;
			int tId = 0;
			int playerNums = 0, pNumLn = 1;
			boolean fLine = true;
			while (r.ready()){
				String l = r.readLine();
				if (fLine){	//Initialize
					try{
						String[] s = l.split(","); //0=w, 1=h, 2=number of player spawns, 3=number of entities at end of file, 4=# of warps
						mapw = Integer.parseInt(s[0]);
						maph = Integer.parseInt(s[1]);
						playerNums = Integer.parseInt(s[2]);
						entitynum = Integer.parseInt(s[3]);
						teleports = Integer.parseInt(s[4]); //generate the teleport array based on number of?
						area = new int[maph][mapw];	//generate the array
						fLine = false;
					}
					catch (Exception e) { System.out.println("Corrupt map file (#101)\n"+ e); }
				}
				else if (pNumLn <= playerNums) {
					if (pNumLn == spawnNum) {
						String[] s = l.split(",");
						if (Integer.parseInt(s[0]) != -1 && Integer.parseInt(s[1]) != -1) {
							p.x = (double)Integer.parseInt(s[0])*48 + 24;
							p.y = (double)Integer.parseInt(s[1])*48 + 24;
						}
						else
							System.out.println("Corrupt map file (#106) -- player spawn incorrect");
					}
					pNumLn++;
				}
				else if (ln < maph) {
					int fill = 0;
					for (int i = 0; i < mapw; i++){
						if (!l.substring(i*2, (i*2)+2).equals("--")) {
							fill = Integer.parseInt(l.substring(i*2, (i*2)+2), 16);
							area[ln][i] = fill;
						}
						else
							area[ln][i] = -1;
					}
					ln++;
				} //end else if
				else if (entitynum > 0) {
					String[] s = l.split(","); //repeats type,x,y,difficulty for each entity
					int entt = Integer.parseInt(s[0]); //type
					int entx = Integer.parseInt(s[1])*48; //x position
					int enty = Integer.parseInt(s[2])*48; //y position
					int entd = Integer.parseInt(s[3]); //difficulty
					e.addNPC(entt, entx, enty, entd);
					entitynum--;
				}
				else if (teleports > 0){
					String[] s = l.split(",");
					int tx = Integer.parseInt(s[0]); //x position (all coords based on tile, not pixel)
					int ty = Integer.parseInt(s[1]); //y position
					String tTo = s[2]; //Destination's map name -- root folder is final/maps/
					int sn = Integer.parseInt(s[3]); //spawn number
					Entity.t[tId] = new Teleport(tx, ty, tTo, sn);
					tId++;
				}
			}	//end while
			r.close();
			mapChanged = true;
			return area;
		}
		catch (Exception e){	System.out.println("Corrupt map file (#102)\n" + e);	return null;	}
	}
	
	public final void minMax() {
		minx = 0;
		miny = 0;
		maxx = mapw-1;
		maxy = maph-1;
		
		if (p.x < w/2+47)
			minx = 0;
		else
			minx = ((int)p.x/48+1)-((w/2)/48+1)-1;
		if (p.y < h/2+47)
			miny = 0;
		else
			miny = ((int)p.y/48+1)-((h/2)/48+1)-1;
		
		maxx = minx + (w/48)+1;
		maxy = miny + (h/48)+1;
		
		if (maxx >= mapw)
			maxx = mapw-1;
		if (maxy >= maph)
			maxy = maph-1;
		if (minx < 0)
			minx = 0;
		if (miny < 0)
			miny = 0;
	}
	
	public final void drawMap(Graphics g2) {
		g2.setColor(Color.black);
		g2.fillRect(0, 0, w, h);
		
		if (mapImageVariables) {
			if (mapChanged) {
				fade = null;
				fade = new boolean[maph][mapw];
				int minx = 0, miny = 0, maxx = mapw-1, maxy = maph-1;
				mapback = null;
				mapback = createImage(mapw*48, maph*48);
				Graphics g3 = mapback.getGraphics();
				int sourcex, sourcey, destx, desty;
				for (int yc = miny; yc <= maxy; yc++) {
					for (int xc = minx; xc <= maxx; xc++) {
						sourcex = (map[yc][xc]%10)*48;
						sourcey = (map[yc][xc]/10)*48;
						destx = (xc*48)/* + (w/2-(int)p.x)*/;
						desty = (yc*48)/* + (h/2-(int)p.y)*/;
						
						if (map[yc][xc] >= 60 && map[yc][xc] < 120)
							sourcey = (map[yc][xc]/10-6)*48;
						else if (map[yc][xc] == 190) {
							sourcex = 192;
							sourcey = 0;
						}
						else if (map[yc][xc] == 191) {
							sourcex = 672;
							sourcey = 48;
						}
						
						if (map[yc][xc] == -1) {
							g3.setColor(Color.black);
							g3.fillRect(destx, desty, 48, 48);
						}
						else
							g3.drawImage(tex, destx, desty, destx+48, desty+48, sourcex, sourcey, sourcex+48, sourcey+48, this);
						
						if ((map[yc][xc] == 3 || map[yc][xc] == 63) && xc > 0 && xc < mapw-1 && yc > 0 && yc < maph-1) {
							if ((map[yc+1][xc] == 3 || map[yc+1][xc] == 63) && (map[yc-1][xc] == 3 || map[yc-1][xc] == 63) && (map[yc][xc+1] == 3 || map[yc][xc+1] == 63) && (map[yc][xc-1] == 3 || map[yc][xc-1] == 63))
								fade[yc][xc] = true;
							else
								fade[yc][xc] = false;
						}
						else
							fade[yc][xc] = false;
					}
				}
				
				for (int yc = 1; yc < maph-1; yc++) {
					for (int xc = 1; xc < mapw-1; xc++) {
						if (fade[yc][xc] == true) {
							fade = waterFade(fade, xc, yc);
							if (fade[yc][xc] == false) { //to check if an adjacent tile's fade should change
								if (fade[yc][xc-1] == true)
									fade = waterFade(fade, xc-1, yc);
								if (fade[yc-1][xc] == true)
									fade = waterFade(fade, xc, yc-1);
								if (fade[yc-1][xc-1] == true)
									fade = waterFade(fade, xc-1, yc-1);
							}
						}
					}
				}
			}
			g2.drawImage(mapback, w/2-(int)p.x, h/2-(int)p.y, this);
		}
		else if (!mapImageVariables) {
			mapback = null;
			Graphics g3 = dbImage.getGraphics();
			g2.setColor(Color.black);
			g2.fillRect(0, 0, w, h);
			int sourcex, sourcey, destx, desty;
			for (int yc = miny; yc <= maxy; yc++) {
				for (int xc = minx; xc <= maxx; xc++) {
					sourcex = (map[yc][xc]%10)*48;
					sourcey = (map[yc][xc]/10)*48;
					destx = (xc*48) + w/2 - (int)p.x;
					desty = (yc*48) + h/2 - (int)p.y;
					
					if (map[yc][xc] >= 60 && map[yc][xc] < 120)
						sourcey = (map[yc][xc]/10-6)*48;
					else if (map[yc][xc] == 190) {
						sourcex = 192;
						sourcey = 0;
					}
					else if (map[yc][xc] == 191) {
						sourcex = 672;
						sourcey = 48;
					}
					
					if (map[yc][xc] == -1) {
						g3.setColor(Color.black);
						g3.fillRect(destx, desty, 48, 48);
					}
					else
						g3.drawImage(tex, destx, desty, destx+48, desty+48, sourcex, sourcey, sourcex+48, sourcey+48, this);
				}
			}
			
			if (mapChanged) {
				fade = null;
				fade = new boolean[maph][mapw];
				for (int yc = 1; yc < maph-1; yc++) {
					for (int xc = 1; xc < mapw-1; xc++) {
						if ((map[yc][xc] == 3 || map[yc][xc] == 63) && xc > 0 && xc < mapw-1 && yc > 0 && yc < maph-1) {
							if ((map[yc+1][xc] == 3 || map[yc+1][xc] == 63) && (map[yc-1][xc] == 3 || map[yc-1][xc] == 63) && (map[yc][xc+1] == 3 || map[yc][xc+1] == 63) && (map[yc][xc-1] == 3 || map[yc][xc-1] == 63))
								fade[yc][xc] = true;
							else
								fade[yc][xc] = false;
						}
						else
							fade[yc][xc] = false;
					}
				}
			}
			int tminx = minx, tminy = miny, tmaxx = maxx, tmaxy = maxy;
			if (tminx < 1)
				tminx = 1;
			if (tminy < 1)
				tminy = 1;
			if (tmaxx > mapw-1)
				tmaxx = mapw-1;
			if (tmaxy > maph-1)
				tmaxy = maph-1;
			for (int yc = tminy; yc <= tmaxy; yc++) {
				for (int xc = tminx; xc <= tmaxx; xc++) {
					if (fade[yc][xc] == true) {
						fade = waterFade(fade, xc, yc);
						if (fade[yc][xc] == false) { //to check if an adjacent tile's fade should change
							if (fade[yc][xc-1] == true)
								fade = waterFade(fade, xc-1, yc);
							if (fade[yc-1][xc] == true)
								fade = waterFade(fade, xc, yc-1);
							if (fade[yc-1][xc-1] == true)
								fade = waterFade(fade, xc-1, yc-1);
						}
					}
				}
			}
		}
		
		for (int i = 0; i < Entity.t.length; i++) { //draw warps
			if (Entity.t[i] != null)
				g2.drawImage(tex, w/2-(int)p.x+Entity.t[i].x*48, h/2-(int)p.y+Entity.t[i].y*48, w/2-(int)p.x+Entity.t[i].x*48+48, h/2-(int)p.y+Entity.t[i].y*48+48, 288, 1200, 336, 1248, this);
		}
	}
	
	public final boolean[][] waterFade(boolean[][] fade, int xc, int yc) { //TODO: fix
		Graphics g3;
		Graphics2D g3d;
		int destx, desty;
		if (mapImageVariables) {
			destx = (xc*48);
			desty = (yc*48);
			g3 = mapback.getGraphics();
			g3d = (Graphics2D)mapback.getGraphics();
			g3.drawImage(tex, destx, desty, destx+48, desty+48, 144, 0, 192, 48, this);
		}
		else {
			destx = (xc*48) + w/2 - (int)p.x;
			desty = (yc*48) + h/2 - (int)p.y;
			g3 = dbImage.getGraphics();
			g3d = (Graphics2D)dbImage.getGraphics();
			g3.drawImage(tex, destx, desty, destx+48, desty+48, 144, 0, 192, 48, this);
		}
		
		int c = 0;
		if (fade[yc+1][xc])
			c++;
		if (fade[yc-1][xc])
			c++;
		if (fade[yc][xc+1])
			c++;
		if (fade[yc][xc-1])
			c++;
		
		if (c < 2) {
			fade[yc][xc] = false;
		}
		else if (c == 2) {
			boolean remove = true;
			if (fade[yc+1][xc] && fade[yc][xc+1] && fade[yc+1][xc+1])
				remove = false;
			if (fade[yc-1][xc] && fade[yc][xc+1] && fade[yc-1][xc+1])
				remove = false;
			if (fade[yc+1][xc] && fade[yc][xc-1] && fade[yc+1][xc-1])
				remove = false;
			if (fade[yc-1][xc] && fade[yc][xc-1] && fade[yc-1][xc-1])
				remove = false;
			
			if (remove)
				fade[yc][xc] = false;
			else { //corner piece
				short ang = 0;
				if (fade[yc+1][xc] && fade[yc][xc+1] && fade[yc+1][xc+1])
					ang = 0;
				if (fade[yc+1][xc] && fade[yc][xc-1] && fade[yc+1][xc-1])
					ang = 1;
				if (fade[yc-1][xc] && fade[yc][xc-1] && fade[yc-1][xc-1])
					ang = 2;
				if (fade[yc-1][xc] && fade[yc][xc+1] && fade[yc-1][xc+1])
					ang = 3;
				
				g3d.translate(destx+24, desty+24);
				g3d.rotate(ang*(PI/2));
				g3d.drawImage(waterfade, -24, -24, 24, 24, 0, 0, 48, 48, this);
			}
		}
		else if (c == 3) { //side piece
			short ang = 0;
			if (!fade[yc-1][xc])
				ang = 0;
			if (!fade[yc][xc+1])
				ang = 1;
			if (!fade[yc+1][xc])
				ang = 2;
			if (!fade[yc][xc-1])
				ang = 3;
			
			g3d.translate(destx+24, desty+24);
			g3d.rotate(ang*(PI/2));
			g3d.drawImage(waterfade, -24, -24, 24, 24, 48, 0, 96, 48, this);
		}
		else { //full block or inside corner
			short corner = 0;
			if (!fade[yc-1][xc-1])
				corner = 1;
			if (!fade[yc-1][xc+1])
				corner = 2;
			if (!fade[yc+1][xc+1])
				corner = 3;
			if (!fade[yc+1][xc-1])
				corner = 4;
			
			if (corner > 0) { //inside corner
				g3d.translate(destx+24, desty+24);
				g3d.rotate((corner-1)*(PI/2));
				g3d.drawImage(waterfade, -24, -24, 24, 24, 96, 0, 144, 48, this);
			}
			else { //full block
				g3.drawImage(waterfade, destx, desty, destx+48, desty+48, 144, 0, 192, 48, this);
			}
		}
		return fade;
	}
	
	public final void drawMap3D(Graphics g2) {
		short[][] drawn = new short[maph][mapw];
		for (int yc = 0; yc < maph; yc++) {
			for (int xc = 0; xc < mapw; xc++) {
				drawn[yc][xc] = 0;
			}
		}
		
		int size = 54; // different layers for walls and objects?
		double xpo = (w/2-p.x) - p.x/(48/(size-48));
		double ypo = (h/2-p.y) - p.y/(48/(size-48));
		int destx, desty, destx2, desty2, destx3, desty3;
		for (int yc = miny; yc <= maxy; yc++) {
			for (int xc = minx; xc <= maxx; xc++) {
				if (map[yc][xc] >= 120) {
					destx = (int)Math.round((xc*size) + xpo);
					desty = (int)Math.round((yc*size) + ypo);
					if (xc+1 <= maxx && map[yc][xc+1] < 120 && p.x > (xc+1)*48+1) {
						if (((drawn[yc][xc]%8)%4)%2 != 1) {
							boolean a = true;
							int r = 1;
							if (map[yc][xc+1] != -1) {
								while(a) {
									if (yc+r <= maxy && map[yc+r][xc] >= 120 && map[yc+r][xc+1] < 120 && map[yc+r][xc+1] != -1) {
										drawn[yc+r][xc] += 1;
										r += 1;
									}
									else
										a = false;
								}
							}
							
							if (r < 2) {
								destx2 = destx + size;
								desty2 = desty + size;
								destx3 = ((xc+1)*48) + (w/2-(int)p.x);
								desty3 = (yc*48) + (h/2-(int)p.y);
								boolean drawblack = false;
								if (map[yc][xc+1] == -1)
									drawblack = true;
								if (destx2-destx3 != 0 && desty2-desty3 != 0) {
									int[] xco = {destx2, destx2, destx3, destx3};
									int[] yco = {desty2, desty, desty3, desty3+48};
									drawImage3D(xco, yco, xc, yc, texdark, drawblack, false);
								}
							}
							else {
								destx2 = destx + size;
								desty2 = desty + (size*r);
								destx3 = ((xc+1)*48) + (w/2-(int)p.x);
								desty3 = (yc*48) + (h/2-(int)p.y);
								if (destx2-destx3 != 0 && desty2-desty3 != 0) {
									int[] xco = {destx2, destx2, destx3, destx3};
									int[] yco = {desty2, desty, desty3, desty3+(48*r)};
									drawWalls3D(xco, yco, xc, yc, r, texdark, false, true);
								}
							}
							drawn[yc][xc] += 1;
						}
					}
					if (xc-1 >= minx && map[yc][xc-1] < 120 && p.x < xc*48) {
						if (((drawn[yc][xc]%8)%4)/2 != 1) {
							boolean a = true;
							int r = 1;
							if (map[yc][xc-1] != -1) {
								while(a) {
									if (yc+r <= maxy && map[yc+r][xc] >= 120 && map[yc+r][xc-1] < 120 && map[yc+r][xc-1] != -1) {
										drawn[yc+r][xc] += 2;
										r += 1;
									}
									else
										a = false;
								}
							}
							
							if (r < 2) {
								destx2 = destx;
								desty2 = desty + size;
								destx3 = (xc*48) + (w/2-(int)p.x);
								desty3 = (yc*48) + (h/2-(int)p.y);
								boolean drawblack = false;
								if (map[yc][xc-1] == -1)
									drawblack = true;
								if (destx2-destx3 != 0 && desty2-desty3 != 0) {
									int[] xco = {destx2, destx2, destx3, destx3};
									int[] yco = {desty2, desty, desty3, desty3+48};
									drawImage3D(xco, yco, xc, yc, texdark, drawblack, false);
								}
							}
							else {
								destx2 = destx;
								desty2 = desty + (size*r);
								destx3 = (xc*48) + (w/2-(int)p.x);
								desty3 = (yc*48) + (h/2-(int)p.y);
								if (destx2-destx3 != 0 && desty2-desty3 != 0) {
									int[] xco = {destx2, destx2, destx3, destx3};
									int[] yco = {desty2, desty, desty3, desty3+(48*r)};
									drawWalls3D(xco, yco, xc, yc, r, texdark, false, true);
								}
							}
							drawn[yc][xc] += 2;
						}
					}
					if (yc+1 <= maxy && map[yc+1][xc] < 120 && p.y > (yc+1)*48+1) {
						if ((drawn[yc][xc]%8)/4 != 1) {
							boolean a = true;
							int r = 1;
								if (map[yc+1][xc] != -1) {
								while(a) {
									if (xc+r <= maxx && map[yc][xc+r] >= 120 && map[yc+1][xc+r] < 120 && map[yc+1][xc+r] != -1) {
										drawn[yc][xc+r] += 4;
										r += 1;
									}
									else
										a = false;
								}
							}
							
							if (r < 2) {
								destx2 = destx + size;
								desty2 = desty + size;
								destx3 = (xc*48) + (w/2-(int)p.x);
								desty3 = ((yc+1)*48) + (h/2-(int)p.y);
								boolean drawblack = false;
								if (map[yc+1][xc] == -1)
									drawblack = true;
								if (destx2-destx3 != 0 && desty2-desty3 != 0) {
									int[] xco = {destx, destx2, destx3+48, destx3};
									int[] yco = {desty2, desty2, desty3, desty3};
									drawImage3D(xco, yco, xc, yc, texdark, drawblack, false);
								}
							}
							else {
								destx2 = destx + (size*r);
								desty2 = desty + size;
								destx3 = (xc*48) + (w/2-(int)p.x);
								desty3 = ((yc+1)*48) + (h/2-(int)p.y);
								if (destx2-destx3 != 0 && desty2-desty3 != 0) {
									int[] xco = {destx, destx2, destx3+(48*r), destx3};
									int[] yco = {desty2, desty2, desty3, desty3};
									drawWalls3D(xco, yco, xc, yc, r, texdark, true, false);
								}
							}
							drawn[yc][xc] += 4;
						}
					}
					if (yc-1 >= miny && map[yc-1][xc] < 120 && p.y < yc*48) {
						if (drawn[yc][xc]/8 != 1) {
							boolean a = true;
							int r = 1;
							if (map[yc-1][xc] != -1) {
								while(a) {
									if (xc+r <= maxx && map[yc][xc+r] >= 120 && map[yc-1][xc+r] < 120 && map[yc-1][xc+r] != -1) {
										drawn[yc][xc+r] += 8;
										r += 1;
									}
									else
										a = false;
								}
							}
							
							if (r < 2) {
								destx2 = destx + size;
								desty2 = desty;
								destx3 = (xc*48) + (w/2-(int)p.x);
								desty3 = (yc*48) + (h/2-(int)p.y);
								boolean drawblack = false;
								if (map[yc-1][xc] == -1)
									drawblack = true;
								if (destx2-destx3 != 0 && desty2-desty3 != 0) {
									int[] xco = {destx2, destx, destx3, destx3+48};
									int[] yco = {desty2, desty2, desty3, desty3};
									drawImage3D(xco, yco, xc, yc, texdark, drawblack, false);
								}
							}
							else {
								destx2 = destx + (size*r);
								desty2 = desty;
								destx3 = (xc*48) + (w/2-(int)p.x);
								desty3 = (yc*48) + (h/2-(int)p.y);
								if (destx2-destx3 != 0 && desty2-desty3 != 0) {
									int[] xco = {destx2, destx, destx3, destx3+(48*r)};
									int[] yco = {desty2, desty2, desty3, desty3};
									drawWalls3D(xco, yco, xc, yc, r, texdark, true, true);
								}
							}
							drawn[yc][xc] += 8;
						}
					}
					drawn[yc][xc] = 15;
				}
			}
		}
		
		for (int i = 0; i < Entity.t.length; i++) { //draw 3D walls of warps
			if (Entity.t[i] != null) {
				int xval = Entity.t[i].x;
				int yval = Entity.t[i].y;
				
				if (xval >= minx && xval <= maxx && yval >= miny && yval <= maxy) { //TODO: fix overhang of other walls
					destx = (int)Math.round((xval*size) + xpo);
					desty = (int)Math.round((yval*size) + ypo);
					destx2 = destx + size;
					desty2 = desty + size;
					destx3 = ((xval+1)*48) + (w/2-(int)p.x);
					desty3 = (yval*48) + (h/2-(int)p.y);
					if (destx2-destx3 != 0 && desty2-desty3 != 0) {
						int[] xco = {destx2, destx2, destx3, destx3};
						int[] yco = {desty2, desty, desty3, desty3+48};
						drawImage3D(xco, yco, xval, yval, tex, false, true);
					}
					
					destx2 = destx;
					desty2 = desty + size;
					destx3 = (xval*48) + (w/2-(int)p.x);
					desty3 = (yval*48) + (h/2-(int)p.y);
					if (destx2-destx3 != 0 && desty2-desty3 != 0) {
						int[] xco = {destx2, destx2, destx3, destx3};
						int[] yco = {desty2, desty, desty3, desty3+48};
						drawImage3D(xco, yco, xval, yval, tex, false, true);
					}
					
					destx2 = destx + size;
					desty2 = desty + size;
					destx3 = (xval*48) + (w/2-(int)p.x);
					desty3 = ((yval+1)*48) + (h/2-(int)p.y);
					if (destx2-destx3 != 0 && desty2-desty3 != 0) {
						int[] xco = {destx, destx2, destx3+48, destx3};
						int[] yco = {desty2, desty2, desty3, desty3};
						drawImage3D(xco, yco, xval, yval, tex, false, true);
					}
					
					destx2 = destx + size;
					desty2 = desty;
					destx3 = (xval*48) + (w/2-(int)p.x);
					desty3 = (yval*48) + (h/2-(int)p.y);
					if (destx2-destx3 != 0 && desty2-desty3 != 0) {
						int[] xco = {destx2, destx, destx3, destx3+48};
						int[] yco = {desty2, desty2, desty3, desty3};
						drawImage3D(xco, yco, xval, yval, tex, false, true);
					}
				}
			}
		}
		
		if (mapImageVariables) {
			if (mapChanged) {
				mapfront = null;
				mapfront = new BufferedImage(mapw*size, maph*size, BufferedImage.TYPE_INT_ARGB_PRE);
				Graphics2D g3d = (Graphics2D)mapfront.getGraphics();
				g3d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				for (int yc = 0; yc <= maph-1; yc++) {
					for (int xc = 0; xc <= mapw-1; xc++) {
						destx = xc*size;
						desty = yc*size;
					if (map[yc][xc] >= 120 && map[yc][xc] < 180) {
							g3d.setColor(Color.black);
							g3d.fillRect(destx, desty, size, size);
						}
						else if (map[yc][xc] >= 180) {
							int sourcex = (map[yc][xc]%10)*48;
							int sourcey = (map[yc][xc]/10)*48;
							if (map[yc][xc] == 190) {
								sourcex = 192;
								sourcey = 0;
							}
							else if (map[yc][xc] == 191) {
								sourcex = 192;
								sourcey = 48;
							}
							g3d.drawImage(tex, destx, desty, destx+size, desty+size, sourcex, sourcey, sourcex+48, sourcey+48, this);
						}
					}
				}
			}
			g2.drawImage(mapfront, (int)Math.round(xpo), (int)Math.round(ypo), this);
		}
		else if (!mapImageVariables) {
			mapfront = null;
			Graphics2D g3d = (Graphics2D)dbImage.getGraphics();
			g3d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			for (int yc = 0; yc <= maph-1; yc++) {
				for (int xc = 0; xc <= mapw-1; xc++) {
					destx = (int)Math.round((xc*size) + xpo);
					desty = (int)Math.round((yc*size) + ypo);
					if (map[yc][xc] >= 120 && map[yc][xc] < 180) {
						g3d.setColor(Color.black);
						g3d.fillRect(destx, desty, size, size);
					}
					else if (map[yc][xc] >= 180) {
						int sourcex = (map[yc][xc]%10)*48;
						int sourcey = (map[yc][xc]/10)*48;
						if (map[yc][xc] == 190) {
							sourcex = 192;
							sourcey = 0;
						}
						else if (map[yc][xc] == 191) {
							sourcex = 192;
							sourcey = 48;
						}
						g3d.drawImage(tex, destx, desty, destx+size, desty+size, sourcex, sourcey, sourcex+48, sourcey+48, this);
					}
				}
			}
		}
	}
	
	public final void drawImage3D(int[] x, int[] y, int xc, int yc, Image a, boolean drawblack, boolean drawwarp) { //3D walls with JAI
		if (!drawblack) {
			BufferedImage temptex = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB_PRE);
			
			if (!drawwarp) {
				int sourcex = (map[yc][xc]%10)*48;
				int sourcey = (map[yc][xc]/10)*48;
				if (map[yc][xc] == 190) {
					sourcex = 192;
					sourcey = 0;
				}
				else if (map[yc][xc] == 191) {
					sourcex = 192;
					sourcey = 48;
				}
				temptex.getGraphics().drawImage(a, 0, 0, 48, 48, sourcex, sourcey, sourcex+48, sourcey+48, this);
			}
			else
				temptex.getGraphics().drawImage(a, 0, 0, 48, 48, 432, 1200, 480, 1248, this);
			
			PerspectiveTransform ptran = PerspectiveTransform.getQuadToQuad(0, 0, 48, 0, 48, 48, 0, 48,	x[0], y[0], x[1], y[1], x[2], y[2], x[3], y[3]);
			
			ParameterBlock pb = (new ParameterBlock()).addSource(temptex);
			try {
				pb.add(new WarpPerspective(ptran.createInverse()));
			//	pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR)); //antialiasing - leaves 'open' lines in corners, and top level of pixels is sometimes missing
			}
			catch (Exception e) { e.printStackTrace(); }
			
			RenderedOp renOp = JAI.create("warp", pb);
			
			((Graphics2D)dbImage.getGraphics()).drawRenderedImage(renOp, new AffineTransform());
		}
		else {
			Graphics g2d = ((Graphics2D)dbImage.getGraphics());
			g2d.setColor(Color.black);
			g2d.fillPolygon(x, y, 4);
		}
	}
	
	public final void drawWalls3D(int[] x, int[] y, int xc, int yc, int num, Image a, boolean horizontal, boolean backwards) { //multiple 3D walls with JAI
		BufferedImage temptex = new BufferedImage(48*num, 48, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics gtt = temptex.getGraphics();
		gtt.setColor(Color.black);
		
		if (horizontal) {
			int j;
			if (backwards)
				j = 48*(num-1);
			else
				j = 0;
			for (int i = xc; i < xc+num; i++) {
				if (map[yc][i] == -1)
					gtt.fillRect(j, 0, 48, 48);
				else {
					int sourcex = (map[yc][i]%10)*48;
					int sourcey = (map[yc][i]/10)*48;
					if (map[yc][i] == 190) {
						sourcex = 192;
						sourcey = 0;
					}
					else if (map[yc][i] == 191) {
						sourcex = 192;
						sourcey = 48;
					}
					
					gtt.drawImage(a, j, 0, 48+j, 48, sourcex, sourcey, sourcex+48, sourcey+48, this);
				}
				if (backwards)
					j -= 48;
				else
					j += 48;
			}
		}
		else {
			int j;
			if (backwards)
				j = 48*(num-1);
			else
				j = 0;
			for (int i = yc; i < yc+num; i++) {
				if (map[i][xc] == -1)
					gtt.fillRect(j, 0, 48, 48);
				else {
					int sourcex = (map[i][xc]%10)*48;
					int sourcey = (map[i][xc]/10)*48;
					if (map[i][xc] == 190) {
						sourcex = 192;
						sourcey = 0;
					}
					else if (map[i][xc] == 191) {
						sourcex = 192;
						sourcey = 48;
					}
					
					gtt.drawImage(a, j, 0, 48+j, 48, sourcex, sourcey, sourcex+48, sourcey+48, this);
				}
				if (backwards)
					j -= 48;
				else
					j += 48;
			}
		}
		
		PerspectiveTransform ptran = PerspectiveTransform.getQuadToQuad(0, 0, 48*num, 0, 48*num, 48, 0, 48, x[0], y[0], x[1], y[1], x[2], y[2], x[3], y[3]);
		
		ParameterBlock pb = (new ParameterBlock()).addSource(temptex);
		try {
			pb.add(new WarpPerspective(ptran.createInverse()));
		//	pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR)); //antialiasing - leaves 'open' lines in corners, and top level of pixels is sometimes missing
		}
		catch (Exception e) { e.printStackTrace(); }
		
		RenderedOp renOp = JAI.create("warp", pb);
		
		((Graphics2D)dbImage.getGraphics()).drawRenderedImage(renOp, new AffineTransform());
		
	/*	Graphics g2d = ((Graphics2D)dbImage.getGraphics());
		g2d.setColor(Color.black);
		g2d.fillPolygon(x, y, 4);
		g2d.setColor(Color.red);
		g2d.drawPolygon(x, y, 4);*/
	}
	
	public final void drawPlayer(Graphics g2) {
	//	Graphics g = g2;
		Graphics2D g2d = (Graphics2D)dbImage.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.translate(w/2, h/2);//p.x, p.y);
		g2d.rotate(prot);
		g2d.drawImage(player, -30, -30, this);
	}
	
	public final void movePlayer() {
		if (running) {
			double speed = 5.1666;
			if (!noclip && p.x > 22 && p.y > 22 && p.x < mapw*48-23 && p.y < maph*48-23)
				if (map[(int)p.y/48][(int)p.x/48] == 3)
					speed *= 0.6667; //water movement
			
			if (!((moveup || movedown) && (moveleft || moveright)))
				speed = Math.sqrt(speed*speed*2);
			if (run)
				speed += 2.33*speed/5;
			
			if (classicControls) {
				if (moveup)
					p.y -= speed;
				if (movedown)
					p.y += speed;
				if (moveleft)
					p.x -= speed;
				if (moveright)
					p.x += speed;
			}
			else {
				if (moveup) {
					p.x += speed*Math.cos(prot);
					p.y += speed*Math.sin(prot);
				}
				if (movedown) {
					p.x -= speed*Math.cos(prot);
					p.y -= speed*Math.sin(prot);
				}
				if (moveleft) {
					p.x += speed*Math.cos(prot-PI/2);
					p.y += speed*Math.sin(prot-PI/2);
				}
				if (moveright) {
					p.x += speed*Math.cos(prot+PI/2);
					p.y += speed*Math.sin(prot+PI/2);
				}
			}
			if (!noclip && p.x > 22 && p.y > 22 && p.x < mapw*48-23 && p.y < maph*48-23)
				playerCollision();
			
			for (int i = 0; Entity.t[i] != null; i++) { //check if in warp
				if ((int)p.x/48 == Entity.t[i].x && (int)p.y/48 == Entity.t[i].y) { //warp to map
					Graphics g = getGraphics();
					g.drawImage(dark, 0, 0, w, h, 0, 0, w, h, this);
					g.drawImage(loadingmap, w/2-loadingmap.getWidth(this)/2, h/2-loadingmap.getHeight(this)/2, this);
				//	if (Entity.t[i].mapTo.charAt(1) == ':')
//local v URL			map = readInMap(Entity.t[i].mapTo, getClass().getResource(""), true, Entity.t[i].sn);
				//	else
						map = readInMap("", getClass().getResource("maps/" + Entity.t[i].mapTo), false, Entity.t[i].sn);
					draw();
					return;
				}
			}
		}
	}
	
	public final void playerCollision () {
		double xc, yc, change = PI/16;
		int xoff = 0, yoff = 0;
		
		if (map[(int)p.y/48][(int)(p.x+21)/48] >= 60 || map[(int)p.y/48][(int)(p.x+21)/48] == -1)
			p.x = ((int)(p.x+21)/48)*48-22;
		if (map[(int)p.y/48][(int)(p.x-21)/48] >= 60 || map[(int)p.y/48][(int)(p.x-21)/48] == -1) //x tip collisions
			p.x = ((int)(p.x-21)/48+1)*48+21;
		
		if (map[(int)(p.y+21)/48][(int)p.x/48] >= 60 || map[(int)(p.y+21)/48][(int)p.x/48] == -1)
			p.y = ((int)(p.y+21)/48)*48-22;
		if (map[(int)(p.y-21)/48][(int)p.x/48] >= 60 || map[(int)(p.y-21)/48][(int)p.x/48] == -1) //y tip collisions
			p.y = ((int)(p.y-21)/48+1)*48+21;
		
		for (double ang = change; ang < 2*PI; ang += change) { //circular collisions
			if (ang != 0 && ang != PI/2 && ang != PI && ang != 3*PI/2 && ang != 2*PI) {
				xc = (21*Math.cos(ang));
				yc = (21*Math.sin(ang));
				xoff = 0;
				yoff = 0;
				if (ang > PI/2 && ang < 3*PI/2) {
					xoff = 1;
				}
				if (ang > PI && ang < 2*PI) {
					yoff = 1;
				}
				if (map[((int)p.y+(int)yc)/48][((int)p.x+(int)xc)/48] >= 60 || map[((int)p.y+(int)yc)/48][((int)p.x+(int)xc)/48] == -1) {
					p.x = ((int)(p.x+xc)/48+xoff)*48-xc;
					p.y = ((int)(p.y+yc)/48+yoff)*48-yc;
				}
			}
		}
	}
	
	public class Proj {
		double x, y;
		double speed;
		double projrot;
		boolean draw;
		Proj next;
		public Proj (double cx, double cy, double destx, double desty, double speedd) {
			x = cx + 19*Math.cos(prot);
			y = cy + 19*Math.sin(prot);
			speed = speedd;
			
			double xc = destx-cx;
			double yc = desty-cy;
			if (xc == 0)
				projrot = yc >= 0? PI/2: -PI/2;
			else {
				projrot = Math.atan(yc/xc);
				if (xc < 0)
					projrot += PI;
			}
			if (projrot < 0)
				projrot += 2*PI;
			
			draw = true;
		}
		public final boolean moveProj() {
			if (draw) {
				x += speed*Math.cos(projrot);
				y += speed*Math.sin(projrot);
				
				//Hit an npc
				if (gotHit(Entity.root, this))
					return true;
				//map-related collision checking
				if (x >= 0 && y >= 0 && x < mapw*48 && y < maph*48 && map[(int)y/48][(int)x/48] < 120 && map[(int)y/48][(int)x/48] != -1) {
					return false;
				}
				else
					return true;
			}
			else
				return true;
		}
		public final boolean gotHit(NPC at, Display.Proj p){
			//try{
				if (at != null){
					if ((p.x < at.x3 && p.x > at.x1) && (p.y < at.y2 && p.y > at.y1)){
						at.takeHealth(5);
						return true;
					}else{
						if (at.next != null)
							return gotHit(at.next, p);
						else
							return false;
					}
				}else	//There are no more npcs left
					return false;
			/*}catch(Exception e){	
				System.out.println("Projectile Error: #401\n" + e);
				return false;
			}*/
		}
	}
	
	public final void addProj(double sx, double sy, double dx, double dy, double speedd) {
		if (rootProj == null) {
			rootProj = new Proj(sx, sy, dx, dy, speedd);
			return;
		}
		Proj runner = rootProj;
		while (true) {
			if (runner.next == null) {
				runner.next = new Proj(sx, sy, dx, dy, speedd);
				return;
			}
			else
				runner = runner.next;
		} //end while
	} //end addProj()
	
	public final void drawEntity(Graphics g2) {
		if (running) {
			//move entities
			if (e.root != null){ 	//Draw NPCs
				e.drawModel(e.root, this, g2, true);
			}
			e.drawWeapons(this, g2);
			
			//move projectiles
			if (rootProj != null) {
				boolean remove = false;
				do {
					remove = rootProj.moveProj();
					if (remove && rootProj.next != null)
						rootProj = rootProj.next;
					else if (remove && rootProj.next == null)
						rootProj = null;
				}
				while (remove && rootProj != null);
				
				Proj runner = rootProj;
				while (runner != null && runner.next != null) {
					runner = runner.next;
					do {
						remove = runner.moveProj();
						if (remove)
							runner.draw = false;
						runner = runner.next;
					}
					while (runner != null);
				}
			}
			//detect collisons
		}
		
		//draw everything
	//	Graphics g2 = getGraphics();
		if (e.root != null){ 	//Draw NPCs
			e.drawModel(e.root, this, g2, false);
		}
		e.drawWeapons(this, g2);
		
		if (rootProj != null) {
			g2.setColor(Color.black);
			Proj runner = rootProj;
			while (runner != null) {
				if (runner.draw)
					g2.fillOval((w/2)-(int)(p.x-runner.x)-2, (h/2)-(int)(p.y-runner.y)-2, 4, 4); //(int)runner.x-2
				runner = runner.next;
			} //end while
		} //end if
		
		//moves/draws entities and projectiles
		//cycle through entities and use draw methods in class
		//if (running) -> move the entity
	}
	
	public final void drawHUD(Graphics g2) {
	//	Graphics g2 = getGraphics();
		if (hudon) {
			if (invopen)
				g2.drawImage(dark, 0, 0, w, h, 0, 0, w, h, this);
			if (mapon)
				drawMiniMap(g2);
			g2.setColor(new Color(200, 200, 200)); //background health/mana
			g2.fillRect(9, h-83, 14, 50);
			g2.fillRect(33, h-23, 50, 14);
			if (p.health > 0) {
				g2.setColor(new Color(191, 0, 0)); //red for health
				g2.fillRect(9, h-33-p.health/2, 14, p.health/2);
			}
			if (p.mana > 0) {
				g2.setColor(new Color(0, 128, 191)); //blue for mana
				g2.fillRect(33, h-23, p.mana/2, 14);
			}
			g2.drawImage(hudinv, 0, h-hudinv.getHeight(this), this);
			g2.setColor(Color.black);
			if (p.q.wid != -1) {
				g2.drawImage(p.q.model, 7, h-hudinv.getHeight(this)+2, this);
				if (p.q.wid == 10) //if it has ammo
					g2.drawString(p.q.ammo + "", 10, h-hudinv.getHeight(this)+15);
			}
			if (p.l.wid != -1) {
				g2.drawImage(p.l.model, 33, h-hudinv.getHeight(this)+60, this);
				if (p.l.wid == 10) //if it has ammo
					g2.drawString(p.l.ammo + "", 36, h-hudinv.getHeight(this)+73);
			}
			if (p.r.wid != -1) {
				g2.drawImage(p.r.model, 91, h-hudinv.getHeight(this)+86, this);
				if (p.r.wid == 10) //if it has ammo
					g2.drawString(p.r.ammo + "", 94, h-hudinv.getHeight(this)+99);
			}
		}
		if (invopen)
			drawInv(g2);
		wepDesc(g2);
		if (!running && !invopen && !p.dead) {
			g2.drawImage(dark, 0, 0, w, h, 0, 0, w, h, this);
			g2.drawImage(paused, w/2-paused.getWidth(this)/2, h/2-paused.getHeight(this)/2, this);
		}
		if (p.dead) {
			g2.drawImage(dark, 0, 0, w, h, 0, 0, w, h, this);
			g2.drawImage(dead, w/2-dead.getWidth(this)/2, h/2-dead.getHeight(this)/2, this);
		}
	}
	
	public final void drawMiniMap(Graphics g2) {
		minimap = createImage(156, 156);
		Graphics g3 = minimap.getGraphics();
		
		int minx = 0, miny = 0, maxx = mapw-1, maxy = maph-1;
		
		g3.setColor(Color.black);
		g3.fillRect(0, 0, 156, 156);
		
		if (mapChanged) {
			minimapback = createImage(mapw*4, maph*4);
			Graphics g4 = minimapback.getGraphics();
			int sourcex, sourcey, destx, desty;
			for (int yc = miny; yc <= maxy; yc++) {
				for (int xc = minx; xc <= maxx; xc++) {
					sourcex = (map[yc][xc]%10)*48;
					sourcey = (map[yc][xc]/10)*48;
					destx = xc*4;
					desty = yc*4;
					if (map[yc][xc] >= 60 && map[yc][xc] < 120)
						sourcey = (map[yc][xc]/10-6)*48;
					if (map[yc][xc] == -1) {
						g4.setColor(Color.black);
						g4.fillRect(destx, desty, 4, 4);
					}
					else
						g4.drawImage(tex, destx, desty, destx+4, desty+4, sourcex, sourcey, sourcex+48, sourcey+48, this);
				}
			}
			g3.drawImage(minimapback, 78-(int)p.x/12, 78-(int)p.y/12, this);
		}
		else
			g3.drawImage(minimapback, 78-(int)p.x/12, 78-(int)p.y/12, this);
	/*	g3.setColor(Color.orange);
		g3.drawRect(0, 0, 156, 156);
		g3.drawRect(1, 1, 154, 154);
		g3.drawRect(2, 2, 152, 152);*/
		g3.setColor(Color.black); //player
		g3.fillRect(76, 76, 4, 4);
		
		g2.drawImage(minimap, w-170, h-170, this);
		g2.drawImage(minimapgfx, w-174, h-174, this);
	}
	
	private void wepDesc(Graphics g2){
		if (!invopen) {
			for (int i = 0; i < Entity.wIndex; i++){
				try{
					if (Entity.w[i].wid != -1){
						int x = (w/2)-(int)(p.x-Entity.w[i].x);
						int y = (h/2)-(int)(p.y-Entity.w[i].y);
						if (mx < x+26 && mx > x && my < y+26 && my > y)	//Mousing over weapon
							drawWepGui(g2, Entity.w[i]);
					}
				}catch(Exception x){	System.out.println("Here: " + x);	}
			}
		}
		else {
			int xstart = w/2-invmain.getWidth(this)/2 + 15;
			int ystart = (h-58)/2-invmain.getHeight(this)/2 + 46;
			if (mx >= xstart && mx < xstart+448 && my >= ystart && my < ystart+224) {
				int invmouse = (mx-xstart)/56 + ((my-ystart)/56)*8;
				if (p.inv[invmouse].wid != -1)
					drawWepGui(g2, p.inv[invmouse]);
			}
			else if (mx >= 0 && mx < hudinv.getWidth(this) && my >= h-hudinv.getHeight(this) && my < h) {
				if (mx >= 7 && mx <= 57 && my >= h-hudinv.getHeight(this)+2 && my <= h-hudinv.getHeight(this)+52) { //if q
					if (p.q.wid > -1)
						drawWepGui(g2, p.q);
				}
				else if (mx >= 33 && mx <= 83 && my >= h-hudinv.getHeight(this)+60 && my <= h-hudinv.getHeight(this)+110) { //if l
					if (p.l.wid > -1)
						drawWepGui(g2, p.l);
				}
				else if (mx >= 91 && mx <= 141 && my >= h-hudinv.getHeight(this)+86 && my <= h-hudinv.getHeight(this)+136) { //if r
					if (p.r.wid > -1)
						drawWepGui(g2, p.r);
				}
			}
		}
	}
	
	public final void drawWepGui(Graphics g, Weapon w2) {
		int x = w-weapdisp.getWidth(this)+5;
		int y = -5;
		g.drawImage(weapdisp, x, y, this);
		g.setColor(Color.black);
		g.drawImage(w2.model, x+198, y+54, this);
		g.drawString("Name  " + w2.name, x+17, y+67);
		g.drawString("Name:", x+18, y+67); //bolds "Name"
		
		g.drawString("Damage  " + w2.damage, x+17, y+27);
		g.drawString("Damage:", x+18, y+27);
		g.drawString("Range  " + w2.range*10 + " pixels", x+17, y+47);
		g.drawString("Range:", x+18, y+47);
		
		if (w2.rate == 1)
			g.drawString("Rate  " + w2.rate + " hit/3s", x+135, y+27);
		else
			g.drawString("Rate  " + w2.rate + " hits/3s", x+135, y+27);
		g.drawString("Rate:", x+136, y+27);
		g.drawString("Durability  " + w2.dur, x+135, y+47);
		g.drawString("Durability:", x+136, y+47);
	}
	
	public final void drawInv(Graphics g2) {
		int xstart = w/2-invmain.getWidth(this)/2;
		int ystart = (h-58)/2-invmain.getHeight(this)/2;
		int xdraw = 18, ydraw = 49;
		g2.drawImage(invmain, xstart, ystart, this);
		for (int i = 0; i < p.inv.length; i++) {
			xdraw = 18;
			ydraw = 49;
			if (p.inv[i].wid != -1) {
				xdraw += (i%8)*56;
				ydraw += (i/8)*56;
				g2.drawImage(p.inv[i].model, xstart+xdraw, ystart+ydraw, this);
				if (p.inv[i].wid == 10) //if it has ammo
					g2.drawString(p.inv[i].ammo + "", xstart+xdraw+3, ystart+ydraw+13);
			//	System.out.println(i + " " + p.inv[i].wid);
			}
		}
		if (invdrag) {
			g2.drawImage(invtemp.model, mx-25, my-25, this);
			if (invtemp.wid == 10) //if it has ammo
				g2.drawString(invtemp.ammo + "", mx-22, my-12);
		}
	}	
	
	public final void draw() {
		dbImage = createImage(w, h);
		g2 = dbImage.getGraphics();
		if (menu == 0) { //in-game
			minMax();
			drawMap(g2);
			drawEntity(g2);
			drawPlayer(g2);
			if (walls3D)
				drawMap3D(g2);
			drawHUD(g2);
			mapChanged = false;
			if (runT) {
				runT = false;
				running = true;
				t = new Thread(new Display.MainLoop());
				t.start();
			}
		}
		g = getGraphics();
		g.drawImage(dbImage, 0, 0, this);
	}
	
	public final void paint(Graphics g) {
		draw();
	} //end paint()
	
	public class MainLoop implements Runnable {
		public MainLoop () { }
		public final void run() {
			while (running) {
				timeb = System.currentTimeMillis();
				if (timeb-timea >= skip) {
					if (moveup || movedown || moveleft || moveright)
						movePlayer();
					draw();
					timea = System.currentTimeMillis();
				} //end if
				else {
					try { Thread.sleep(skip/16); }
					catch(Exception e) { e.printStackTrace(); }
				} //end else
			} //end while
			draw();
		} //end run()
	} //end nested class MainLoop
} //end class Display