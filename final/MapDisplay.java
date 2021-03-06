import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.*;
import javax.imageio.*;
import javax.media.jai.*;
import javax.swing.JOptionPane;

//TODO:
//selecting and highlighting entity -> entity becomes popup with drop-down list to choose
//entity adding

public class MapDisplay extends Applet implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	
	public static int w, h; //w is the width of the applet, h is the height
	int mx, my, mpx, mpy, prx, pry;
	public static int entnum = 0; //number of entities
	int[] box = new int[2]; //right-clicking make a box
	int[][] copy;
	public static int[][] map;
	public static int maph = 0;
	public static int mapw = 0;
	public static int px, py; //view x and y
	public static int[] plx, ply; //xs and ys of player spawns
	public static Teleport[] t = new Teleport[30];
	public static int tnum = 0; //number of teleporters -- playerSpawnNum, but for warps
	public static int zoom = 1;
	int[] selected = new int[2]; //0 is x, 1 is y
	public static boolean mapin = false;
	public static boolean delete = false, deletepw = false;
	public static boolean drawDiag = false;
	boolean draw3d = false;
	boolean first = true;
	Image tex, texdark, sel, tileset;
	Image dbImage;
	BufferedImage mapfront;
	Graphics g2;
	public static int playerSpawnNum = 0;
	public static String entityTemp = "";
	
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
		selected[0] = 0;
		selected[1] = 0;
		px = 0;
		py = 0;
		
		plx = new int[10]; //-1 = clear
		ply = new int[10];
		for (int i = 0; i < plx.length; i++) {
			plx[i] = -1;
			ply[i] = -1;
		}
		playerSpawnNum = 0;
		
		box[0] = -1;
		box[1] = -1;
		addMouseListener(this);
		addMouseMotionListener(this);
		try { //load all images
			tex = ImageIO.read(getClass().getResource("gfx/textures.png"));
			texdark = ImageIO.read(getClass().getResource("gfx/texturesdark.png"));
			sel = ImageIO.read(getClass().getResource("gfx/tileselected.png"));
		}
		catch(Exception e) { e.printStackTrace(); }
		
		this.requestFocus();
	} //end init()
	
	public final void mouseEntered(MouseEvent e) {
		draw();
		requestFocusInWindow();
	}
	public final void mouseExited(MouseEvent e) { }
	public final void mouseClicked(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		
		e.consume();
	}
	public final void mousePressed(MouseEvent e) {
		mpx = e.getX();
		mpy = e.getY();
		prx = mpx;
		pry = mpy;
		e.consume();
	}
	public final void mouseReleased(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		if (Math.abs(mx-prx) <= 3 && Math.abs(my-pry) <= 3)
			clickComm(e);
		draw();
	}
	public final void mouseMoved(MouseEvent e) { }
	public final void mouseDragged(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		if (Math.abs(mx-prx) > 3)
			px += mx-mpx;
		if (Math.abs(my-pry) > 3)
			py += my-mpy;
		mpx = mx;
		mpy = my;
		draw();
		e.consume();
	}
	
	public static final void clearMap() { //clears variables when loading a map or making a new one
		playerSpawnNum = 0;
		tnum = 0;
		t = null;
		t = new Teleport[20];
		entityTemp = "";
	}
	
	public void clickComm(MouseEvent e) { //TODO: delete entity/warp/player? -- like "delete tiles"?
		if (mx > w-181 && my < 469) { //if in the tile selection
			selected[0] = (mx-(w-182))/18;
			selected[1] = (my+1)/18;
			if (selected[0] > 4 && selected[1] == 25) {
				if (selected[0] == 5) //player
					selected[0] = 101;
				else if (selected[0] == 6) //warp
					selected[0] = 102;
				else if (selected[0] == 7) { //entity
					selected[0] = 103;
					//TODO: trigger popup to select entity
				}
			}
			if (selected[1] > 25)
				selected[1] = 25;
		}
		else if (mapin && mx < w-181 && selected[0] != -1 && selected[1] != -1 && (my-py)/(48/zoom) >= 0 && (my-py)/(48/zoom) < maph && (mx-px)/(48/zoom) >= 0 && (mx-px)/(48/zoom) < mapw) { //if in the map viewer
			if (deletepw) {
				int tempx = (mx-px)/(48/zoom);
				int tempy = (my-py)/(48/zoom);
				for (int i = 0; i < t.length; i++) { //delete warps
					if (t[i] != null && t[i].x == tempx && t[i].y == tempy) {
						t[i] = null;
					}
				}
				for (int i = 0; i < plx.length; i++) { //delete player spawns
					if (plx[i] == tempx && ply[i] == tempy) {
						plx[i] = -1;
						ply[i] = -1;
						playerSpawnNum--;
					}
				}
			}
			else if (selected[0] == 101) { //player
				boolean conflict = false;
				int tempx = (mx-px)/(48/zoom);
				int tempy = (my-py)/(48/zoom);
				for (int i = 0; i < t.length; i++) { //conflicts with warps
					if (t[i] != null && t[i].x == tempx && t[i].y == tempy) {
						conflict = true;
					}
				}
				for (int i = 0; i < plx.length; i++) { //conflicts with other player spawns
					if (plx[i] == tempx && ply[i] == tempy) {
						conflict = true;
					}
				}
				if (conflict) {
					System.out.println("conflict");
					return;
				}
				
				for (int i = 0; i < plx.length; i++) { //place spawn point
					if (plx[i] == -1 && ply[i] == -1) {
						plx[i] = tempx;
						ply[i] = tempy;
						playerSpawnNum++;
						draw();
						return;
					}
				}
				System.out.println("too many player spawns");
			}
			else if (selected[0] == 102) {
				placeWarp((mx-px)/(48/zoom), (my-py)/(48/zoom));
			} //warps
			else if (selected[0] == 103) { } //entity
			else if (e.getButton() == MouseEvent.BUTTON1) {
				int tile = selected[0] + selected[1]*10;
				if (delete)
					tile = -1;
				map[(my-py)/(48/zoom)][(mx-px)/(48/zoom)] = tile;
			//	System.out.println((mx-px)/(48/zoom) + "x y" + (my-py)/(48/zoom));
			}
			else if (e.getButton() == MouseEvent.BUTTON3) {
				int tx = (mx-px)/(48/zoom);
				int ty = (my-py)/(48/zoom);
				int tile = selected[0] + selected[1]*10;
				if (delete)
					tile = -1;
				if (box[0] == -1 && box[1] == -1) {
					box[0] = tx;
					box[1] = ty;
				}
				else {
					if (drawDiag) { //draw diagonal lines only
						if (ty >= box[1]) {
							for (int y = box[1], yc = 0; y <= ty; y++) {
								if (tx >= box[0]) {
									for (int x = box[0], xc = 0; x <= tx; x++) {
										if (xc == yc)
											map[y][x] = tile;
										xc++;
									}
								}
								else if (tx < box[0]) {
									for (int x = box[0], xc = 0; x >= tx; x--) {
										if (xc == yc)
											map[y][x] = tile;
										xc++;
									}
								}
								yc++;
							}
						}
						else if (ty < box[1]) {
							for (int y = box[1], yc = 0; y >= ty; y--) {
								if (tx >= box[0]) {
									for (int x = box[0], xc = 0; x <= tx; x++) {
										if (xc == yc)
											map[y][x] = tile;
										xc++;
									}
								}
								else if (tx < box[0]) {
									for (int x = box[0], xc = 0; x >= tx; x--) {
										if (xc == yc)
											map[y][x] = tile;
										xc++;
									}
								}
								yc++;
							}
						}
					}
					else { //fills box
						if (ty >= box[1]) {
							for (int y = box[1]; y <= ty; y++) {
								if (tx >= box[0]) {
									for (int x = box[0]; x <= tx; x++) {
										map[y][x] = tile;
									}
								}
								else if (tx < box[0]) {
									for (int x = box[0]; x >= tx; x--) {
										map[y][x] = tile;
									}
								}
							}
						}
						else if (ty < box[1]) {
							for (int y = box[1]; y >= ty; y--) {
								if (tx >= box[0]) {
									for (int x = box[0]; x <= tx; x++) {
										map[y][x] = tile;
									}
								}
								else if (tx < box[0]) {
									for (int x = box[0]; x >= tx; x--) {
										map[y][x] = tile;
									}
								}
							}
						}
					}
					box[0] = -1;
					box[1] = -1;
				}
			}
		}
		draw();
	}
	
	public final void placeWarp(int coordx, int coordy) {
		boolean b = false; //true if a warp's already there
		for (int i = 0; i < t.length; i++) { //check other warps
			if (t[i] != null && coordx == t[i].x && coordy == t[i].y)
				b = true;
		}
		for (int i = 0; i < plx.length; i++) { //check player spawns
			if (plx[i] == coordx && ply[i] == coordy) {
				System.out.println("conflict with a player spawn");
				return;
			}
		}
		
		if (!b) { //placing the warp
			//popup to set map and spawn number
			int spawnNum = 1;
			String mapTo;
			String a = (String)JOptionPane.showInputDialog(MapCreator.frame, "Enter map name (base folder is \"final\\maps\\<mapname>\") and the spawn number, separated by a comma (no space)\nExample: \"test\\testname.map,1\" -- map directory of \"final\\maps\\test\\testmap.map\" with the first spawn number", "New Warp", JOptionPane.PLAIN_MESSAGE, null, null, "test\\testname.map,1");
			if (a != null) {
				String[] str = a.split(",");
				if (str[0].charAt(0) == '\\')
					mapTo = str[0].substring(1);
				else
					mapTo = str[0];
				try {
					spawnNum = Integer.parseInt(str[1]);
				}
				catch(Exception e) { e.printStackTrace(); return; }
				
				for (int i = 0; i < t.length; i++) { //adds new warp
					if (t[i] == null) {
						t[i] = new Teleport(coordx, coordy, mapTo, spawnNum);
						tnum++;
						return;
					}
				}
			}
		}
		else { //edit warp that's already there
			int ind = 0;
			for (int i = 0; i < t.length; i++) {
				if (t[i] != null && coordx == t[i].x && coordy == t[i].y)
					ind = i;
			}
			int spawnNum = t[ind].sn;
			String mapTo = t[ind].mapTo;
			String a = (String)JOptionPane.showInputDialog(MapCreator.frame, "Enter map name (base folder is \"final\\maps\\<mapname>\") and the spawn number, separated by a comma (no space)\nExample: \"test\\testname.map,1\" -- map directory of \"final\\maps\\test\\testmap.map\" with the first spawn number", "New Warp", JOptionPane.PLAIN_MESSAGE, null, null, mapTo + "," + spawnNum);
			if (a != null) {
				String[] str = a.split(",");
				if (str[0].charAt(0) == '\\')
					mapTo = str[0].substring(1);
				else
					mapTo = str[0];
				try {
					spawnNum = Integer.parseInt(str[1]);
				}
				catch(Exception e) { e.printStackTrace(); return; }
				
				t[ind] = new Teleport(coordx, coordy, mapTo, spawnNum);
				return;
			}
		}
	}
	
	public static final void saveMap(File filename) {
		if (playerSpawnNum > 0) {
			try {
				PrintWriter outputfile = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
				outputfile.println(mapw + "," + maph + "," + plx.length + "," + entnum + "," + tnum);
				
				for (int i = 0; i < plx.length; i++) //player spawns
					outputfile.println(plx[i] + "," + ply[i]);
				
				for (int i = 0; i < maph; i++) { //map
					for (int j = 0; j < mapw; j++) {
						if (map[i][j] < 16 && map[i][j] != -1)
							outputfile.print("0");
						
						if (map[i][j] == -1)
							outputfile.print("--");
						else
							outputfile.print(Integer.toHexString(map[i][j]).toUpperCase());
					}
					outputfile.println();
				} //end for
				
				//entities
				String[] tempE = entityTemp.split("-");
				for (int i = 0; i < entnum; i++) {
					outputfile.println(tempE[i]);
					System.out.println(tempE[i]);
				}
				
				for (int i = 0; i < t.length; i++) { //warps
					if (t[i] != null) {
						outputfile.println(t[i].x + "," + t[i].y + "," + t[i].mapTo + "," + t[i].sn);
					}
				}
				
				outputfile.close();	//closes file
				JOptionPane.showMessageDialog(MapCreator.frame, "Save successful!");
			} //end try
			catch(Exception e) {
				System.out.println(e);
				JOptionPane.showMessageDialog(MapCreator.frame, "Save unsuccessful.", "Not Saved", JOptionPane.ERROR_MESSAGE);
			} //end catch
		}
		else {
			JOptionPane.showMessageDialog(MapCreator.frame, "You need at least one player spawn.", "No Player Spawn(s)", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static final int[][] readInMap(File fn) {
		int[][] area = null;
		clearMap();
		try{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
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
						String[] s = l.split(","); //0=w, 1=h, 2=number of player spawns, 3=number of entities at end of file, 4=# of teleports
						mapw = Integer.parseInt(s[0]);
						maph = Integer.parseInt(s[1]);
						playerNums = Integer.parseInt(s[2]);
						entnum = Integer.parseInt(s[3]);
						entitynum = Integer.parseInt(s[3]);
						teleports = Integer.parseInt(s[4]);
						px = 0;
						py = 0;
						area = new int[maph][mapw];	//Generate the array
						fLine = false;
					}
					catch (Exception e) { System.out.println("Corrupt map file (#101)\n"+ e); }
				}
				else if (pNumLn <= playerNums) {
					String[] s = l.split(",");
					plx[pNumLn-1] = Integer.parseInt(s[0]);
					ply[pNumLn-1] = Integer.parseInt(s[1]);
					if (Integer.parseInt(s[0]) != -1 && Integer.parseInt(s[1]) != -1)
						playerSpawnNum++;
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
				/*	String[] s = l.split(","); //repeats x,y,type,health,damage,range for each entity
					int entt = Integer.parseInt(s[0]); //type
					int entx = Integer.parseInt(s[1])*48; //x position
					int enty = Integer.parseInt(s[2])*48; //y position
					int entd = Integer.parseInt(s[3]); //difficulty
					e.addNPC(entt, entx, enty, entd);*/
					entityTemp += l + "-";
					entitynum--;
				}
				else if (teleports > 0){
					String[] s = l.split(",");
					int tx = Integer.parseInt(s[0]); //x position (all coords based on tile, not pixel)
					int ty = Integer.parseInt(s[1]); //y position
					String tTo = s[2]; //Destination's map name -- root folder is final/maps/
					int sn = Integer.parseInt(s[3]); //spawn number
					t[tId] = new Teleport(tx, ty, tTo, sn);
					tnum++;
					tId++;
				}
			}	//end while
			r.close();
			mapin = true;
			return area;
		}
		catch (Exception e){	System.out.println("Corrupt map file (#102)\n" + e); return null;	}
	}
	
	public static final int[][] readInOldMap(File fn) {
		int[][] area = null;
		clearMap();
		try{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
			int entitynum = 0;
			int ln = 0;
			boolean fLine = true;
			while (r.ready()){
				String l = r.readLine();
				if (fLine){	//Initialize
					try{
						String[] s = l.split(","); //0=w, 1=h, 2=p.x, 3=p.y, 4=number of entities at end of file
						mapw = Integer.parseInt(s[0]);
						maph = Integer.parseInt(s[1]);
						plx[0] = Integer.parseInt(s[2]);
						ply[0] = Integer.parseInt(s[3]);
						playerSpawnNum = 1;
						px = 0;
						py = 0;
						entnum = Integer.parseInt(s[4]);
						entitynum = Integer.parseInt(s[4]);
						area = new int[maph][mapw];	//Generate the array
						fLine = false;
					}catch (Exception e){	System.out.println("Corrupt map file (#101)\n"+ e);	}
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
				/*	String[] s = l.split(","); //repeats x,y,type,health,damage,range for each entity
					int entt = Integer.parseInt(s[0]); //type
					int entx = Integer.parseInt(s[1])*48; //x position
					int enty = Integer.parseInt(s[2])*48; //y position
					int entd = Integer.parseInt(s[3]); //difficulty
					e.addNPC(entt, entx, enty, entd);*/
					entityTemp += l + "-";
					entitynum--;
				}
			}	//end while
			r.close();
			mapin = true;
			return area;
		}
		catch (Exception e){	System.out.println("Corrupt map file (#102)\n" + e); return null;	}
	}
	
	public int[] drawMap(Graphics g2) {
		//	Graphics g2 = getGraphics();
		if (mapin) {
			int minx = 0, miny = 0, maxx = mapw-1, maxy = maph-1;
			
			if (0-px < 2*(48/zoom))
				minx = 0;
			else
				minx = (0-px)/(48/zoom)-1;
			if (0-py < 2*(48/zoom))
				miny = 0;
			else
				miny = (0-py)/(48/zoom)-1;
			
			maxx = minx + w/(48/zoom)+1;
			maxy = miny + h/(48/zoom)+2;
			
			if (maxx >= mapw)
				maxx = mapw-1;
			if (maxy >= maph)
				maxy = maph-1;
			if (minx < 0)
				minx = 0;
			if (miny < 0)
				miny = 0;
			
			g2.setColor(Color.black);
			g2.fillRect(0, 0, w, h);
			
			int sourcex, sourcey, destx, desty;
			for (int yc = miny; yc <= maxy; yc++) {
				for (int xc = minx; xc <= maxx; xc++) {
					sourcex = (map[yc][xc]%10)*48;
					sourcey = (map[yc][xc]/10)*48;
					destx = (xc*48)/zoom+px;
					desty = (yc*48)/zoom+py;
					if (map[yc][xc] != -1)
						g2.drawImage(tex, destx, desty, destx+(48/zoom), desty+(48/zoom), sourcex, sourcey, sourcex+48, sourcey+48, this);
				}
			}
			
			if (box[0] != -1 && box[1] != -1) { //box selection
				g2.setColor(Color.red);
				g2.drawRect(((box[0]*48)/zoom+px), ((box[1]*48)/zoom+py), 48/zoom, 48/zoom);
				g2.drawRect(((box[0]*48)/zoom+px)+1, ((box[1]*48)/zoom+py)+1, 48/zoom-2, 48/zoom-2);
				g2.drawRect(((box[0]*48)/zoom+px)+2, ((box[1]*48)/zoom+py)+2, 48/zoom-4, 48/zoom-4);
			}
			for (int i = 0; i < t.length; i++) { //draw warps
				if (t[i] != null) {
					g2.drawImage(tex, (t[i].x*48)/zoom+px, (t[i].y*48)/zoom+py, (t[i].x*48)/zoom+px+48, (t[i].y*48)/zoom+py+48, 288, 1200, 336, 1248, this);
				}
			}
			for (int i = 0; i < plx.length; i++) { //draw player spawns
				if (plx[i] != -1 && ply[i] != -1) {
					destx = (plx[i]*48)/zoom+px;
					desty = (ply[i]*48)/zoom+py;
					g2.drawImage(tex, destx, desty, destx+(48/zoom), desty+(48/zoom), 240, 1200, 288, 1248, this);
					g2.setColor(Color.black);
					g2.drawString(""+(i+1), destx+21, desty+29);
				}
			}
			int[] mmxy = {minx, miny, maxx, maxy};
			return mmxy;
		}
		else {
			int s = w-181;
			g2.setColor(Color.black);
			g2.fillRect(0, 0, s-1, h-1);
			g2.setColor(Color.red);
			g2.drawRect(0, 0, s-1, h-1);
			return null;
		}
	}
	
	public final void drawTile(Graphics g2) {
		int s = w-181;
	//	Graphics g2 = getGraphics();
		g2.setColor(Color.black);
		g2.fillRect(s, 0, 181, h);
		if (first) {
			tileset = createImage(181, 528);
			Graphics g3 = tileset.getGraphics();
			g3.setColor(Color.black);
			g3.fillRect(0, 0, 181, 528);
			int sourcex, sourcey, destx, desty;
			for (int y = 0; y < 26; y++) {
				for (int x = 0; x < 10; x++) {
					sourcex = x*48;
					sourcey = y*48;
					destx = (x*18)+1;
					desty = (y*18)+1;
					if (x < 8 || y != 25)
						g3.drawImage(tex, destx, desty, destx+17, desty+17, sourcex, sourcey, sourcex+48, sourcey+48, this);
				}
			}
			first = false;
		}
		g2.drawImage(tileset, s, 0, this);
		
		if (selected[0] == 101) //player
			g2.drawImage(sel, 90+s, 450, this);
		else if (selected[0] == 102) //warp
			g2.drawImage(sel, 108+s, 450, this);
		else if (selected[0] == 103) //entity
			g2.drawImage(sel, 126+s, 450, this);
		else if (selected[0] != -1 && selected[1] != -1)
			g2.drawImage(sel, (selected[0]*18)+s, (selected[1]*18), this);
	}
	
	public final void draw() {
		Graphics g = getGraphics();
		dbImage = createImage(w, h);
		g2 = dbImage.getGraphics();
		int[] mmxy; //0minx, 1miny, 2maxx, 3maxy
		mmxy = drawMap(g2);
		if (mapin && draw3d)
			drawMap3D(g2, mmxy);
		drawTile(g2);
		g.drawImage(dbImage, 0, 0, this);
	}
	
	public final void paint(Graphics g) {
		draw();
	} //end paint()
	
	public final void drawMap3D(Graphics g2, int[] mmxy) {
		int minx = mmxy[0], miny = mmxy[1], maxx = mmxy[2], maxy = mmxy[3];
		int plx = -px + (w-181)/2;
		int ply = -py + h/2;
		int size = 54/zoom;
		double xpo = ((w-181)/2-plx) - plx/((48/zoom)/(size-(48/zoom)));
		double ypo = (h/2-ply) - ply/((48/zoom)/(size-(48/zoom)));
		int destx, desty, destx2, desty2, destx3, desty3;
		for (int yc = miny; yc <= maxy; yc++) {
			for (int xc = minx; xc <= maxx; xc++) {
				if (map[yc][xc] >= 120 || map[yc][xc] == -1) {
					destx = (int)Math.round((xc*size) + xpo);
					desty = (int)Math.round((yc*size) + ypo);
					if (xc+1 <= maxx && map[yc][xc+1] < 120 && map[yc][xc+1] != -1 && plx > (xc+1)*(48/zoom)+1) {
						destx2 = destx + size;
						desty2 = desty + size;
						destx3 = ((xc+1)*(48/zoom)) + ((w-181)/2-(int)plx);
						desty3 = (yc*(48/zoom)) + (h/2-(int)ply);
						boolean drawblack = false;
						if (map[yc][xc] == -1)
							drawblack = true;
						if (destx2-destx3 != 0 && desty2-desty3 != 0) {
							int[] xco = {destx2, destx2, destx3, destx3};
							int[] yco = {desty2, desty2-size, desty3, desty3+(48/zoom)};
							drawImage3D(xco, yco, xc, yc, texdark, drawblack);
						}
					}
					if (xc-1 >= minx && map[yc][xc-1] < 120 && map[yc][xc-1] != -1 && plx < xc*(48/zoom)) {
						destx2 = destx;
						desty2 = desty + size;
						destx3 = (xc*(48/zoom)) + ((w-181)/2-(int)plx);
						desty3 = (yc*(48/zoom)) + (h/2-(int)ply);
						boolean drawblack = false;
						if (map[yc][xc] == -1)
							drawblack = true;
						if (destx2-destx3 != 0 && desty2-desty3 != 0) {
							int[] xco = {destx2, destx2, destx3, destx3};
							int[] yco = {desty2, desty2-size, desty3, desty3+(48/zoom)};
							drawImage3D(xco, yco, xc, yc, tex, drawblack);
						}
					}
					if (yc+1 <= maxy && map[yc+1][xc] < 120 && map[yc+1][xc] != -1 && ply > (yc+1)*(48/zoom)+1) {
						destx2 = destx + size;
						desty2 = desty + size;
						destx3 = (xc*(48/zoom)) + ((w-181)/2-(int)plx);
						desty3 = ((yc+1)*(48/zoom)) + (h/2-(int)ply);
						boolean drawblack = false;
						if (map[yc][xc] == -1)
							drawblack = true;
						if (destx2-destx3 != 0 && desty2-desty3 != 0) {
							int[] xco = {destx2-size, destx2, destx3+(48/zoom), destx3};
							int[] yco = {desty2, desty2, desty3, desty3};
							drawImage3D(xco, yco, xc, yc, texdark, drawblack);
						}
					}
					if (yc-1 >= miny && map[yc-1][xc] < 120 && map[yc-1][xc] != -1 && ply < yc*(48/zoom)) {
						destx2 = destx + size;
						desty2 = desty;
						destx3 = (xc*(48/zoom)) + ((w-181)/2-(int)plx);
						desty3 = (yc*(48/zoom)) + (h/2-(int)ply);
						boolean drawblack = false;
						if (map[yc][xc] == -1)
							drawblack = true;
						if (destx2-destx3 != 0 && desty2-desty3 != 0) {
							int[] xco = {destx2, destx2-size, destx3, destx3+(48/zoom)};
							int[] yco = {desty2, desty2, desty3, desty3};
							drawImage3D(xco, yco, xc, yc, tex, drawblack);
						}
					}
				}
			}
		}
		mapfront = new BufferedImage(mapw*size, maph*size, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g3d = (Graphics2D)mapfront.getGraphics();
		g3d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		for (int yc = 0; yc <= maph-1; yc++) {
			for (int xc = 0; xc <= mapw-1; xc++) {
				destx = xc*size;
				desty = yc*size;
				if (map[yc][xc] == -1) {
					g3d.setColor(Color.black);
					g3d.fillRect(destx, desty, size, size);
				}
				else if (map[yc][xc] >= 120) {
					int sourcex = (map[yc][xc]%10)*48;
					int sourcey = (map[yc][xc]/10)*48;
					g3d.drawImage(tex, destx, desty, destx+size, desty+size, sourcex, sourcey, sourcex+48, sourcey+48, this);
				}
			}
		}
		g2.drawImage(mapfront, (int)Math.round(xpo), (int)Math.round(ypo), this);
	}
	
	public final void drawImage3D(int[] x, int[] y, int xc, int yc, Image a, boolean drawblack) { //3D walls with JAI
		if (!drawblack) {
			BufferedImage temptex = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB_PRE);
			temptex.getGraphics().drawImage(a, 0, 0, 48, 48, (map[yc][xc]%10)*48, (map[yc][xc]/10)*48, (map[yc][xc]%10)*48+48, (map[yc][xc]/10)*48+48, this);
			
			PerspectiveTransform ptran = PerspectiveTransform.getQuadToQuad(0, 0, 48, 0, 48, 48, 0, 48,	x[0], y[0], x[1], y[1], x[2], y[2], x[3], y[3]);
			
			ParameterBlock pb = (new ParameterBlock()).addSource(temptex);
			try {
				pb.add(new WarpPerspective(ptran.createInverse()));
			//	pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR)); //antialiasing - leaves 'open' lines between textures
			}
			catch (Exception e) { e.printStackTrace(); }
			
			RenderedOp renOp = JAI.create("warp", pb);
			
			((Graphics2D)dbImage.getGraphics()).drawRenderedImage(renOp, new AffineTransform());
		}
		else {
			g2.setColor(Color.black);
			((Graphics2D)dbImage.getGraphics()).fillPolygon(x, y, 4);
		}
	}
} //end class Display