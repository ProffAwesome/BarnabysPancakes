import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public class MapComps extends JPanel implements ActionListener/*, MenuElement*/ {
	private static final long serialVersionUID = 1L;
	
	public static int wid = 853; //width and height of the inside
	public static int hei = 480; //content (not including the menu)
	public static MapDisplay display = new MapDisplay() { private static final long serialVersionUID = 1L; { init(hei, wid); }};
	
	File loadfile = null;
	public static JMenuBar menubar = new JMenuBar();
	JMenu file;
	JMenuItem newmap, load, save, saveas, exit, mapsize, replace, diag, zoomin, zoomout, delete;
	JFileChooser fc = new JFileChooser(); //file chooser window
	
	boolean read = false; //if data is read
	
	public MapComps(JFrame frame) {
		frame.setLayout(new SpringLayout());
		frame.setContentPane(display);
		
		fc.setFileFilter(new mapFileFilter());
		makeMenu();
		frame.setJMenuBar(menubar);
		
		frame.setMinimumSize(new Dimension(656, 528));
		frame.setPreferredSize(new Dimension(wid+16, hei+59));
	} //end constructor
	
	public void makeMenu() {
		file = new JMenu("File");
		
		newmap = new JMenuItem("New Map");
		load = new JMenuItem("Load Map");
		save = new JMenuItem("Save Map");
		saveas = new JMenuItem("Save Map As");
		exit = new JMenuItem("Exit");
		mapsize = new JMenuItem("Reize Map");
		replace = new JMenuItem("Replace Tiles");
		diag = new JMenuItem("Diagonal Line");
		zoomin = new JMenuItem("Zoom In");
		zoomout = new JMenuItem("Zoom Out");
		delete = new JMenuItem("Delete Tiles");
		
		file.add(newmap);
		file.add(load);
		file.add(save);
		file.add(saveas);
		file.add(exit);
		
		menubar.add(file);
		menubar.add(mapsize);
		menubar.add(replace);
		menubar.add(diag);
		menubar.add(zoomin);
		menubar.add(zoomout);
		menubar.add(delete);
		
		newmap.addActionListener(this);
		load.addActionListener(this);
		save.addActionListener(this);
		saveas.addActionListener(this);
		exit.addActionListener(this);
		mapsize.addActionListener(this);
		replace.addActionListener(this);
		diag.addActionListener(this);
		zoomin.addActionListener(this);
		zoomout.addActionListener(this);
		delete.addActionListener(this);
	}
	
	/**
	 * Reads in the text from a file.
	 * 
	 * @param	fileIn	the file being read in from
	 */
	public String readIn(File fileIn) {
		String temp = null;
		try {
			temp = "";
		//	int v = 0;
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn)));
			while(in.ready() == true) {
				temp = temp + in.readLine() + "\n";
			}
			read = true;
		} //end try
		catch(Exception e){
			System.out.println(e);
			read = false;
		} //end catch
		return temp;
	} //end readIn()
	
	public void resizeMap(int ow, int oh, boolean newmap) {
		String a = (String)JOptionPane.showInputDialog(MapComps.this, "Enter the width and height, separated by a comma (no space)\nExample: 15,18 --> makes it 15 block wide, 18 tall", "New Map Size", JOptionPane.PLAIN_MESSAGE, null, null, ow + "," + oh);
		if (a != null) {
			String[] nwh = a.split(",");
			int nw, nh;
			try {
				nw = Integer.parseInt(nwh[0]);
				nh = Integer.parseInt(nwh[1]);
			}
			catch(Exception e) { System.out.println(e); return; }
			
			int[][] maporig = MapDisplay.map;
			MapDisplay.mapw = nw;
			MapDisplay.maph = nh;
			MapDisplay.map = new int[nh][nw];
			if (newmap) {
				for (int ay = 0; ay < nh; ay++) {
					for (int ax = 0; ax < nw; ax++) {
						MapDisplay.map[ay][ax] = 0;
					}
				}
				MapDisplay.px = 0;
				MapDisplay.py = 0;
				MapDisplay.playerx = -1;
				MapDisplay.playery = -1;
				loadfile = null;
			}
			else {
				if (nh <= oh) {
					for (int ay = 0; ay < nh; ay++) {
						if (nw <= ow) {
							for (int ax = 0; ax < nw; ax++) {
								MapDisplay.map[ay][ax] = maporig[ay][ax];
							}
						}
						else {
							for (int ax = 0; ax < ow; ax++) {
								MapDisplay.map[ay][ax] = maporig[ay][ax];
							}
							for (int ax = ow; ax < nw; ax++) {
								MapDisplay.map[ay][ax] = 0;
							}
						}
					}
				}
				else {
					for (int ay = 0; ay < oh; ay++) {
						if (nw <= ow) {
							for (int ax = 0; ax < nw; ax++) {
								MapDisplay.map[ay][ax] = maporig[ay][ax];
							}
						}
						else {
							for (int ax = 0; ax < ow; ax++) {
								MapDisplay.map[ay][ax] = maporig[ay][ax];
							}
							for (int ax = ow; ax < nw; ax++) {
								MapDisplay.map[ay][ax] = 0;
							}
						}
					}
					for (int ay = oh; ay < nh; ay++) {
						for (int ax = 0; ax < nw; ax++) {
							MapDisplay.map[ay][ax] = 0;
						}
					}
				}
			}
			MapDisplay.mapin = true;
		}
	}
	
	public void replaceTiles() {
		String a = (String)JOptionPane.showInputDialog(MapComps.this, "Enter the block to be replaced and the replacing block, separated by a comma (no space)\nExample: 0,60 --> replaces all grey floors into dark-grey bricks\n-1 counts as deleted blocks", "Replace Tiles", JOptionPane.PLAIN_MESSAGE, null, null, 0 + "," + 60);
		if (a != null) {
			String[] nwh = a.split(",");
			int oldt, newt;
			try {
				oldt = Integer.parseInt(nwh[0]);
				newt = Integer.parseInt(nwh[1]);
			}
			catch(Exception e) { System.out.println(e); return; }
			
			if (oldt != newt) {
				if (oldt >= -1 && oldt < 255 && newt >= -1 && newt < 255) {
					for (int ay = 0; ay < MapDisplay.maph; ay++) {
						for (int ax = 0; ax < MapDisplay.mapw; ax++) {
							if (MapDisplay.map[ay][ax] == oldt)
								MapDisplay.map[ay][ax] = newt;
						}
					}
				}
			}
		}
	}
	
	public void actionPerformed(ActionEvent avt) {
		if (avt.getSource() == newmap) {
			resizeMap(1, 1, true);
			loadfile = null;
			display.draw();
		}
		else if (avt.getSource() == load) {
			int returnVal = fc.showOpenDialog(MapComps.this); //whether 'OK' or 'Cancel' was pressed in the dialog box
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				loadfile = file;
				MapDisplay.map = MapDisplay.readInMap(file);
			} //end if
			display.draw();
		}
		else if (avt.getSource() == save) {
			if (loadfile != null)
				MapDisplay.saveMap(loadfile);
			else
				saveAs();
		}
		else if (avt.getSource() == saveas) {
			saveAs();
		}
		else if (avt.getSource() == mapsize) {
			if (MapDisplay.mapin)
				resizeMap(MapDisplay.mapw, MapDisplay.maph, false);
			display.draw();
		}
		else if (avt.getSource() == replace) {
			if (MapDisplay.mapin)
				replaceTiles();
			display.draw();
		}
		else if (avt.getSource() == diag) {
			if (MapDisplay.drawDiag) {
				MapDisplay.drawDiag = false;
				diag.setSelected(false);
			}
			else {
				MapDisplay.drawDiag = true;
				diag.setSelected(true);
			}
		}
		else if (avt.getSource() == zoomin) {
			if (MapDisplay.zoom > 1) {
				MapDisplay.zoom /= 2;
				MapDisplay.px = (MapDisplay.px*2) - ((MapDisplay.w-181)/2);
				MapDisplay.py = (MapDisplay.py*2) - (MapDisplay.h/2);
			}
			display.draw();
		}
		else if (avt.getSource() == zoomout) {
			if (MapDisplay.zoom < 16) {
				MapDisplay.zoom *= 2;
				MapDisplay.px = (MapDisplay.px/2) + ((MapDisplay.w-181)/4);
				MapDisplay.py = (MapDisplay.py/2) + (MapDisplay.h/4);
			}
			display.draw();
		}
		else if (avt.getSource() == delete) {
			if (MapDisplay.delete) {
				MapDisplay.delete = false;
				delete.setSelected(false);
			}
			else {
				MapDisplay.delete = true;
				delete.setSelected(true);
			}
		}
		else if (avt.getSource() == exit) {
			MapCreator.close();
		}
	} //end actionPerformed()
	
	/**
	 * Nested class to set the FIleFilter for the JFileChooser (fc).
	 * Uses the '.map' extension.
	 */
	public class mapFileFilter extends FileFilter {
		public boolean accept(File f) {
			if(f.isDirectory())
				return true;
			return f.getName().endsWith(".map");
		}
		public String getDescription() {
			return "Barnaby's Pancakes Map files (*.map)";
		}
	}
	public final void saveAs() {
		int returnVal = fc.showSaveDialog(MapComps.this); //whether 'OK' or 'Cancel' was pressed in the dialog box
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String path = file.getAbsolutePath();
			if(!path.endsWith(".map"))
				file = new File(path + ".map");
			loadfile = file;
			MapDisplay.saveMap(file);
		} //end if
	}
} //end class