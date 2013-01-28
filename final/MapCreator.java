//import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//19 impassible tiles to edge

public class MapCreator {
	static JFrame frame = new JFrame("Barnaby's Pancakes - Map Creator");
	//
	/**
	 * Makes window and adds components - uses comps.java.
	 */
	public static void makeGUI() {
		//JFrame frame = new JFrame("Shader test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //closing the window stops it
		
		final MapComps a4 = new MapComps(frame); //makes the components from the 'comps' class by creating an object
		frame.add(a4);
		frame.addWindowListener(new WindowAdapter() {
	        public void windowActivated(WindowEvent e) {
	        	a4.requestFocusInWindow();
	        }
	    });
		frame.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent arg0) {
				int widt = frame.getWidth()-16;
				int heig = frame.getHeight()-59;
				MapDisplay.w = widt;
				MapDisplay.h = heig;
			}
			public void componentHidden(ComponentEvent arg0) { }
			public void componentMoved(ComponentEvent arg0) { }
			public void componentShown(ComponentEvent arg0) { }
		});
		frame.pack();			//
		frame.setVisible(true);	// displays the window
	} //end makeGUI()
	
	/**
	 * Closes the window/program.
	 */
	public static void close() {
		frame.dispose();
	}
	
	/**
	 * Main class.
	 * 
	 * Creates and displays a GUI, which uses a sorting and searching library to
	 *  sort and search arrays.
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				makeGUI();
			}
		});
	} //end main()
} //end class