import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;

public class comps extends JPanel implements ActionListener/*, MenuElement*/ {
	private static final long serialVersionUID = 1L;
	
	public static int wid = 853; //width and height of the inside
	public static int hei = 480; //content (not including the menu)
	public static Display display = new Display() { private static final long serialVersionUID = 1L; { init(hei, wid); }};
	
//	JTextArea display = new JTextArea();
	
	public static JMenuBar menubar = new JMenuBar();
	JMenu file, options, dev, help;
	JMenuItem mainmenu, load, save, exit;
	
	JMenu controls; //all the options and such
	JRadioButton classContr, newContr;
	JCheckBox hudOn, mapOn;
	JMenuItem update1;
	
	JCheckBox noclip; //dev
	JMenuItem loadmap, update2;
	
	JMenuItem howtoplay, aboutBP; //help
	
	JFileChooser fc = new JFileChooser(); //file chooser window
	
	boolean read = false; //if data is read
	
	public comps(JFrame frame) {
		frame.setLayout(new SpringLayout());
		frame.setContentPane(display);
	//	frame.add(display); //adds applet
		
		makeMenu();
		frame.setJMenuBar(menubar);
		
		//frame -> applet: -16x, -59y
		//frame -> applet with menu: -16x, -36y
		
	//	display.setPreferredSize(new Dimension(wid, hei));
		frame.setMinimumSize(new Dimension(656, 419));
		frame.setPreferredSize(new Dimension(wid+16, hei+59));
	} //end constructor
	
	public void makeMenu() {
		update1 = new JMenuItem("Update Settings");
		update2 = new JMenuItem("Update Dev Settings");
		update1.addActionListener(this);
		update2.addActionListener(this);
		
		file = new JMenu("File");
		mainmenu = new JMenuItem("Main Menu");
		load = new JMenuItem("Load Game");
		save = new JMenuItem("Save Game");
		exit = new JMenuItem("Exit");
		file.add(mainmenu);
		file.add(load);
		file.add(save);
		file.add(exit);
		mainmenu.addActionListener(this);
		load.addActionListener(this);
		save.addActionListener(this);
		exit.addActionListener(this);
		menubar.add(file);
		
		
		options = new JMenu("Options"); //TODO: make this have a popup to set controls
			controls = new JMenu("Controls");
			classContr = new JRadioButton("Classic Controls");
			newContr = new JRadioButton("Relative Controls");
			ButtonGroup b1 = new ButtonGroup();
			b1.add(classContr);
			b1.add(newContr);
			newContr.setSelected(true);
			controls.add(classContr);
			controls.add(newContr);
			controls.add(new JSeparator());
			options.add(controls);
			
			hudOn = new JCheckBox("HUD");
			hudOn.setSelected(true);
			mapOn = new JCheckBox("MiniMap");
			mapOn.setSelected(true);
			options.add(hudOn);
			options.add(mapOn);
			options.add(new JSeparator());
			options.add(update1);
		menubar.add(options);
		
		dev = new JMenu("Dev");
		noclip = new JCheckBox("Noclip");
		noclip.setSelected(false);
		dev.add(noclip);
		loadmap = new JMenuItem("Load Map");
		dev.add(loadmap);
		dev.add(new JSeparator());
		dev.add(update2);
		loadmap.addActionListener(this);
		if (!Display.demo)
			menubar.add(dev);
		
		help = new JMenu("Help");
		howtoplay = new JMenuItem("How To Play");
		aboutBP = new JMenuItem("About Barnaby's Pancakes");
		help.add(howtoplay);
		help.add(aboutBP);
		howtoplay.addActionListener(this);
		aboutBP.addActionListener(this);
		menubar.add(help);
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
	
	public void actionPerformed(ActionEvent avt) {
		if (avt.getSource() == mainmenu) {
			Display.menu = 1;
			display.draw();
		}
		else if (avt.getSource() == load) {
			int returnVal = fc.showOpenDialog(comps.this); //whether 'OK' or 'Cancel' was pressed in the dialog box
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile(); //file path
			//	readInMap(file); - make binary file for saved games
			//	String data = readIn(file); //reads in the file
			} //end if
			else {
				;
			}
			display.draw();
		}
		else if (avt.getSource() == save) {
			//TODO
		}
		else if (avt.getSource() == exit) {
			//TODO: dialog - "do you want to save before exit?"
			GUIBuild.close();
		}
		else if (avt.getSource() == loadmap) {
			int returnVal = fc.showOpenDialog(comps.this); //whether 'OK' or 'Cancel' was pressed in the dialog box
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Display.map = null;
				Display.p = new Player(true);
				Display.map = Display.readInMap(fc.getSelectedFile().getAbsolutePath(), getClass().getResource(""), true, 1);
			} //end if
			display.draw();
		}
		
		
		if (classContr.isSelected())
			Display.classicControls = true;
		else if (newContr.isSelected())
			Display.classicControls = false;
		
		if (hudOn.isSelected())
			Display.hudon = true;
		else if (!hudOn.isSelected())
			Display.hudon = false;
		
		if (mapOn.isSelected())
			Display.mapon = true;
		else if (!mapOn.isSelected())
			Display.mapon = false;
		
		if (noclip.isSelected())
			Display.noclip = true;
		else if (!noclip.isSelected())
			Display.noclip = false;
	} //end actionPerformed()
	
	public final void updateSettings() {
		
	}
} //end class