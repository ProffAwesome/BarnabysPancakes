import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.JOptionPane;

//TODO:
//selecting and highlighting entity -> entity becomes popup with drop-down list to choose
//entity adding

public class MapDisplay extends Applet implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	
	public static int w, h; //w is the width of the applet, h is the height
	int mx, my, mpx, mpy;
	public static int entnum = 0; //number of entities
	int[] box = new int[2]; //right-clicking make a box
	int[][] copy;
	public static int[][] map;
	public static int maph = 0;
	public static int mapw = 0;
	public static int px, py; //view x and y
	public static int playerx, playery; //x and y of player spawn
	public static int zoom = 1;
	int[] selected = new int[2]; //0 is x, 1 is y
	public static boolean mapin = false;
	public static boolean delete = false;
	public static boolean drawDiag = false;
	boolean first = true;
	Image tex, sel, plytile, playsel, tileset;
	Image dbImage;
	Graphics g2;
	
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
		playerx = -1;
		playery = -1;
		box[0] = -1;
		box[1] = -1;
		addMouseListener(this);
		addMouseMotionListener(this);
		try { //load all images
			tex = ImageIO.read(new File("gfx/textures.png"));
			sel = ImageIO.read(new File("gfx/tileselected.png"));
			plytile = ImageIO.read(new File("gfx/player_entity_tiles.png"));
			playsel = ImageIO.read(new File("gfx/playerselected.png"));
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
		if (mx > w-181 && my < 469) { //if in the tile selection
			selected[0] = (mx-(w-182))/18;
			selected[1] = (my+1)/18;
			if (selected[0] > 4 && selected[1] == 25) {
				if ((mx-(w-182))/9 > 9 && (mx-(w-182))/9 < 15)
					selected[0] = 32;
			/*	else if ((mx-(w-182))/9 > 14 && (mx-(w-182))/9 < 21) {
					//selected[0] = 64;
					//TODO: entity popup and choice
				}*/
			}
			if (selected[1] > 25)
				selected[1] = 25;
			//drawTile();
			draw();
		}
		else if (mapin && mx < w-181 && selected[0] != -1 && selected[1] != -1 && (my-py)/(48/zoom) >= 0 && (my-py)/(48/zoom) < maph && (mx-px)/(48/zoom) >= 0 && (mx-px)/(48/zoom) < mapw) { //if in the map viewer
			if (selected[0] == 32) {
				playerx = (mx-px)/(48/zoom);
				playery = (my-py)/(48/zoom);
			}
			else if (e.getButton() == MouseEvent.BUTTON1) {
				int tile = selected[0] + selected[1]*10;
				if (delete)
					tile = -1;
				map[(my-py)/(48/zoom)][(mx-px)/(48/zoom)] = tile;
			//	System.out.println((((my-py)/48)/zoom) + " " + (((mx-px)/48)/zoom));
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
			draw();
		}
		e.consume();
	}
	public final void mousePressed(MouseEvent e) {
		mpx = e.getX();
		mpy = e.getY();
		e.consume();
	}
	public final void mouseReleased(MouseEvent e) { }
	public final void mouseMoved(MouseEvent e) { }
	public final void mouseDragged(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		px += mx-mpx;
		py += my-mpy;
		mpx = mx;
		mpy = my;
		draw();
		//repaint();
		e.consume();
	}
	
	public static final void saveMap(File filename) {
		//TODO: check that everything's good --> playerx/y, etc.
		//TODO: pop up dialog box if checks not successful
		try {
			PrintWriter outputfile = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			outputfile.println(mapw + "," + maph + "," + playerx + "," + playery + "," + entnum);
			for (int i = 0; i < maph; i++) {
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
			outputfile.close();	//closes file
			JOptionPane.showMessageDialog(MapCreator.frame, "Save successful!");
		} //end try
		catch(Exception e) {
			System.out.println(e);
			JOptionPane.showMessageDialog(MapCreator.frame, "Save unsuccessful.", "Not Saved", JOptionPane.ERROR_MESSAGE);
		} //end catch
	}
	
	public static final int[][] readInMap(File fn) {
		int[][] area = null;
		try{
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
			int ln = 0;
			boolean fLine = true;
			while (r.ready()){
				String l = r.readLine();
				if (fLine){	//Initialize
					try{
						String[] s = l.split(","); //0=w, 1=h, 2=p.x, 3=p.y, 4=number of entities at end of file
						mapw = Integer.parseInt(s[0]);
						maph = Integer.parseInt(s[1]);
						playerx = Integer.parseInt(s[2]);
						playery = Integer.parseInt(s[3]);
						px = 0;
						py = 0;
						entnum = Integer.parseInt(s[4]);
						area = new int[maph][mapw];	//Generate the array
						fLine = false;
					}catch (Exception e){	System.out.println("Corrupt map file (#101)\n"+ e);	}
				}else{
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
				}	//end if
			}	//end while
			r.close();
			mapin = true;
			return area;
		}
		catch (Exception e){	System.out.println("Corrupt map file (#102)\n" + e); return null;	}
	}
	
	public void drawMap(Graphics g2) {
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
			maxy = miny + h/(48/zoom)+1;
			
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
			if (playerx != -1 && playery != -1) { //player position
				g2.setColor(Color.green);
				g2.drawRect((playerx*48)/zoom+px, (playery*48)/zoom+py, 48/zoom-1, 48/zoom-1);
				g2.fillRect((playerx*48+8)/zoom+px, (playery*48+8)/zoom+py, 32/zoom, 32/zoom);
				//TODO: draw player graphic
			}
		}
		else {
			int s = w-181;
			g2.setColor(Color.black);
			g2.fillRect(0, 0, s-1, h-1);
			g2.setColor(Color.red);
			g2.drawRect(0, 0, s-1, h-1);
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
					if (x < 5 || y != 25)
						g3.drawImage(tex, destx, desty, destx+17, desty+17, sourcex, sourcey, sourcex+48, sourcey+48, this);
					else if (x == 5 && y == 25)
						g3.drawImage(plytile, destx, desty, 89, 17, this);
				}
			}
			first = false;
		}
		g2.drawImage(tileset, s, 0, this);
		
		if (selected[0] == 32)
			g2.drawImage(playsel, 90+s, 450, this);
		else if (selected[0] != -1 && selected[1] != -1)
			g2.drawImage(sel, (selected[0]*18)+s, (selected[1]*18), this);
	}
	
	public final void draw() {
		Graphics g = getGraphics();
		dbImage = createImage(w, h);
		g2 = dbImage.getGraphics();
		drawMap(g2);
		drawTile(g2);
		g.drawImage(dbImage, 0, 0, this);
	}
	
	public final void paint(Graphics g) {
		draw();
	} //end paint()
} //end class Display