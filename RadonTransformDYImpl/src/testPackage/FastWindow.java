package testPackage;

import javax.swing.JFrame;

public class FastWindow {

	public static int WIDTH = 0;
	public static int HEIGHT = 0;

	/**
	 * the driver code for the panel and radon stuff (don't touch)
	 * 
	 * @param args - not used, don't touch
	 */
	public static void main(String args[]) {
		JFrame theWindow = new JFrame("Window");
		FastPanel panel = new FastPanel();
		theWindow.setSize(WIDTH, HEIGHT);
		theWindow.setResizable(false);
		theWindow.setLocationRelativeTo(null);
		theWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theWindow.add(panel);
		panel.start();
		theWindow.setVisible(true);
	}

}
