package testPackage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
//achen:: importing required sharpening libraries
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

@SuppressWarnings("serial")
public class FastPanel extends JPanel {

	// fields
	private double maxAveFinal;
	private BufferedImage image;
	private BufferedImage newImage;
	private BufferedImage finalImage;
	private Graphics2D graphic;
	private Graphics2D g2;
	private Graphics2D finalGraphic;
	private String path = "";
	private static final GraphicsConfiguration configu = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice().getDefaultConfiguration();

	private static double sensitivity = 0; // Initial 0.4

	/**
	 * a private method which returns the nearest power of two to the inputted
	 * value, used to find the nearest size to pad the image
	 * 
	 * @param side - a int of the inputted value
	 * @return an int which is the closest power of two greater than side
	 */
	private int powerOfTwo(int side) {
		int power = 0;
		while (side > (Math.pow(2, power))) {
			power += 1;
		}
		return ((int) Math.pow(2, power));
	}

	/**
	 * a constructor for the FastPanel class the constructor pulls the image from
	 * the file path, finds its derivative, erodes it, and pads it to a 2^n x 2^n
	 * square
	 */
	public FastPanel() {
		try {
			path = "C:\\Users\\achen\\Desktop\\Fall '19\\Master's Thesis\\Radon Transform Code\\Masters Thesis\\Masters Thesis\\Code\\BU Implementation version\\Radon\\images\\sofa.jpg";
			System.out.println(path);
			image = ImageIO.read(new File(path));
			graphic = (Graphics2D) image.getGraphics();
			FastWindow.WIDTH = image.getWidth();
			FastWindow.HEIGHT = image.getHeight();
			int width = image.getWidth();
			int height = image.getHeight();

			//
			// Take the derivative
			//
			Morph derivative = new Morph(image);
			double[][] derivArray = derivative.findDerivative();

			//
			// Draw the derivative
			//
			for (int x = 0; x < width - 2; x++) {
				for (int y = 0; y < height - 2; y++) {
					double arrayValue = derivArray[x][y];
					int rgbNum = (int) Math.floor(arrayValue);
					try {
						Color color = new Color(rgbNum, rgbNum, rgbNum);
						graphic.setColor(color);
						graphic.fillRect(x, y, 1, 1);
					} catch (IllegalArgumentException a) {
						try {
							int newRGB = (int) Math.floor(rgbNum * 0.25);
							Color color = new Color(newRGB, newRGB, newRGB);
							graphic.setColor(color);
							graphic.fillRect(x, y, 1, 1);
						} catch (IllegalArgumentException b) {
							// some values fall outside of 0 to 255 range, doesn't affect the image
						}
					}
				}
			}

			//
			// Widen the lines 3x3 kernel
			//
			Morph erosion = new Morph(image);
			double[][] array = erosion.erode();
			for (int x = 1; x < width - 2; x++) {
				for (int y = 1; y < height - 2; y++) {
					double arrayValue = array[x][y];
					int rgbNum = (int) Math.floor(arrayValue);
					try {
						Color color = new Color(rgbNum, rgbNum, rgbNum);
						graphic.setColor(color);
						graphic.fillRect(x, y, 1, 1);
					} catch (IllegalArgumentException e) {
						System.out.println(rgbNum + " is not a valid value");
					}
				}
			}

			//
			// padding out the image and fixing the white line left by the derivative
			//
			int control = Math.max(width, height);
			int newN = powerOfTwo(control);
			newImage = configu.createCompatibleImage(newN, newN);
			g2 = (Graphics2D) newImage.getGraphics();
			g2.drawImage(image, 0, 0, null);
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, image.getWidth() - 1, 5);
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, 10, image.getHeight() - 1);
			g2.setColor(Color.BLACK);
			g2.fillRect(0, image.getHeight() - 2, image.getWidth() - 1, 5);
			g2.setColor(Color.BLACK);
			g2.fillRect(image.getWidth() - 10, 0, 10, image.getHeight() - 1);

		} catch (IOException e) {
			System.out.println("IOException");
		} catch (NullPointerException n) {
			System.out.println("File name " + path + " does not exist.");
		}

	}

	/**
	 * a method which calls the radon on the image, and calls makeLine to draw on
	 * the edges. Can also construct the sinogram map
	 */
	public void start() {

		// Calculate the radon transform using O N^2 lg N with ADRT algorithm
		int n = (newImage.getWidth()) / 2;
		int maxRho = (int) Math.floor(Math.sqrt((n * n) + (n * n)));
		double[][] table = radon(newImage, maxRho, 360);

		// make a backup of the table
		double[][] origtable = new double[maxRho][360];
		for (int r = 0; r < maxRho; r++)
			for (int t = 0; t < 360; t++)
				origtable[r][t] = table[r][t];

		// achen:: blur image changes
		System.out.println("Blurring table\n");
		double newtable[][] = new double[maxRho][360];

		int blur_strength = 250;
		for (int i = 0; i < blur_strength; i++) {
			for (int rho1 = 1; rho1 < maxRho - 1; rho1++) {
				for (int theta = 1; theta < 360 - 1; theta++) {
					double blurred = (4 * table[rho1][theta] + table[rho1 + 1][theta] + table[rho1][theta + 1]
							+ table[rho1 - 1][theta] + table[rho1][theta - 1]) / 8;
					newtable[rho1][theta] = Math.max(blurred, table[rho1][theta]);
				}
			}

			for (int rho1 = 1; rho1 < maxRho; rho1++) {
				for (int theta = 1; theta < 360; theta++) {
					table[rho1][theta] = newtable[rho1][theta];
				}
			}

		}
		int blur = 350;
		for (int i = 0; i < blur; i++) {
			for (int rho1 = 1; rho1 < maxRho - 1; rho1++) {
				for (int theta = 1; theta < 360 - 1; theta++) {
					double blurred = (4 * table[rho1][theta] + table[rho1 + 1][theta] + table[rho1][theta + 1]
							+ table[rho1 - 1][theta] + table[rho1][theta - 1]) / 8;
					newtable[rho1][theta] = blurred;
				}
			}

			for (int rho1 = 1; rho1 < maxRho; rho1++) {
				for (int theta = 1; theta < 360; theta++) {
					table[rho1][theta] = newtable[rho1][theta];
				}
			}
		}
		// end achen:: blur image changes

		/*
		 * / achen:: alternate smooth image changes
		 * System.out.println("Smoothening table\n"); double newtable1[][] = new
		 * double[maxRho][360]; //int maxNeigh = max value of 9 neighbours; int
		 * smoothen_strength = 60; for (int i=0;i<smoothen_strength;i++) { for (int rho1
		 * = 1; rho1 < maxRho-1; rho1++) { for (int theta = 1; theta < 360-1; theta++) {
		 * double maxNeigh =
		 * Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(table[rho1+1][theta+1],
		 * table[rho1][theta]),table[rho1+1][theta]),table[rho1][theta+1]),table[rho1-1]
		 * [theta]),table[rho1][theta-1]),table[rho1-1][theta-1]);
		 * newtable1[rho1][theta] = Math.max(maxNeigh-50, table[rho1][theta]); } }
		 * //newtable1[rho][theta] = max(0.97 * maxNeigh, table[rho][theta]; for (int
		 * rho1 = 1; rho1 < maxRho; rho1++) { for (int theta = 1; theta < 360; theta++)
		 * { table[rho1][theta] = newtable1[rho1][theta]; } } } //end achen:: smoothen
		 * pixel changes
		 */

		// makeLine(table);
		// ArrayList<Pixel> pixelList = makeLine(newtable);
		ArrayList<Pixel> pixelList = new ArrayList<Pixel>();
		int maxRho1 = table.length;
		int maxDegs = table[0].length;
		double percent = maxAveFinal * sensitivity;

		// Original version using sensitivity
		/*
		 * // takes out pixels within the threshold, and then removes those which are
		 * too // close together for (int outerRho = 0; outerRho < maxRho; outerRho++) {
		 * for (int outerDegs = 0; outerDegs < maxDegs; outerDegs++) { double currentVal
		 * = table[outerRho][outerDegs]; if (currentVal >= (maxAveFinal - percent)) {
		 * Pixel bigPixel = new Pixel(outerRho, outerDegs);
		 * 
		 * if (suppress(pixelList, bigPixel)) {
		 * 
		 * } else { pixelList.add(bigPixel); } } } }
		 */

		// Pure Non-max suppress
		for (int rho = 2; rho < maxRho1 - 2; rho++) {
			for (int deg = 2; deg < maxDegs - 2; deg++) {
				if (table[rho][deg] > table[rho - 1][deg - 1] && table[rho][deg] > table[rho - 1][deg]
						&& table[rho][deg] > table[rho - 1][deg + 1] && table[rho][deg] > table[rho][deg - 1]
						&& table[rho][deg] > table[rho][deg + 1] && table[rho][deg] > table[rho + 1][deg - 1]
						&& table[rho][deg] > table[rho + 1][deg] && table[rho][deg] > table[rho + 1][deg + 1]) {
					Pixel bigPixel = new Pixel(rho, deg);
					pixelList.add(bigPixel);
				}
			}
		}
		
		// create backup of pixelList
		ArrayList<Pixel> origPixelList = new ArrayList<Pixel>();
		for ( int i= 0;i<pixelList.size();i++) {
			origPixelList.add(pixelList.get(i));
		}

		// achen: Create Map of Pixel
		int clustermap[][] = new int[maxRho][360];
		int kmax = 1;
		for (int i = 0; i < kmax; i++) {
			for (int rho2 = 1; rho2 < maxRho - 1; rho2++) {
				for (int theta2 = 1; theta2 < 360 - 1; theta2++) {
					for (int c = 0; c < pixelList.size(); c++) {
						double a = rho2 - pixelList.get(c).x;
						double b = theta2 - pixelList.get(c).y;
						double dist = (a * a) + (b * b);
						dist = Math.sqrt(dist);
						double d = rho2 - pixelList.get(clustermap[rho2][theta2]).x;
						double e = theta2 - pixelList.get(clustermap[rho2][theta2]).y;
						// System.out.println("d = "+d);
						// System.out.println("e = "+e);
						double prevDist = (d * d) + (e * e);
						prevDist = Math.sqrt(prevDist);
						if (dist < prevDist) {
							clustermap[rho2][theta2] = c;
							// System.out.printf("clustermap = " + clustermap[rho2][theta2]);
						}
					}
				}
			}

			for (int rho2 = 1; rho2 < maxRho; rho2++) {
				for (int theta2 = 1; theta2 < 360; theta2++) {
					int c = clustermap[rho2][theta2];
					// System.out.println("c = " + c);
					double a = origtable[rho2][theta2];
					double b = origtable[pixelList.get(c).x][pixelList.get(c).y];

					if (a > b) {
						pixelList.get(c).x = rho2;
						pixelList.get(c).y = theta2;
					}
				}
			}
		}
		// achen: end map of pixel changes

		// makeLine
		makeLine(newtable, pixelList);

		// the below code draws the sinogram
		//double percent = maxAveFinal * sensitivity;
		finalImage = configu.createCompatibleImage(maxRho, 360);
		finalGraphic = finalImage.createGraphics();

		for (int degs = 0; degs < 360; degs++) {
			for (int rho = 0; rho < maxRho; rho++) {
				// double val = table[rho][degs];
				//double val = newtable[rho][degs];
				double val = origtable[rho][degs];
				// achen:: print table values with (rho, degs) coordinates
				//System.out.println(val);
				double part1 = val / maxAveFinal;
				double part2 = part1 * 255;
				int colorVal = (int) Math.floor(part2);
				Color color = new Color(colorVal, colorVal, colorVal);
				/*
				 * if (val >= (maxAveFinal - percent)) { System.out.println("Rho = " + rho +
				 * ", Deg = " + degs); finalGraphic.setColor(Color.RED);
				 * //finalGraphic.fillRect(rho, degs, 5, 5) finalGraphic.fillRect(rho - 1, degs
				 * - 1, 1, 1); }
				 */

				finalGraphic.setColor(color);
				finalGraphic.fillRect(rho, degs, 1, 1);
			}
		}

		// achen:: create file to write rho and theta values
		try {
			File file = new File("C:\\Users\\achen\\Desktop\\Fall '19\\Master's Thesis\\Radon Transform Code\\test.txt");
			if(file.createNewFile()) {
				System.out.println("Successfully created");
			}
			else {
				System.out.println("Failed to create or file already exist");
			}
			FileWriter fileWriter = new FileWriter(file);
			System.out.println("Writing fields to file:: ");
			fileWriter.write(String.format("%s\t%s\t%s\n", "rho", "theta", "pixel"));
			System.out.println("Writing data to file:: ");
			
		for (int i = 0; i < origPixelList.size(); i++) {
			Pixel maxRhoTheta = origPixelList.get(i);
			int rh = maxRhoTheta.x;
			int degrees = maxRhoTheta.y;
			double val = origtable[rh][degrees];

			System.out.println("rho = " + rh);
			System.out.println("theta = " + degrees);
			System.out.println("Pixel Value = " + val);
			// achen:: write rho and theta to file
			fileWriter.write(String.format("%d\t%d\t\t%f\n", rh, degrees, val));
			fileWriter.flush();
			finalGraphic.setColor(Color.BLUE);
			finalGraphic.fillRect(rh - 1, degrees - 1, 4, 4);
		}
		fileWriter.close();
		
		}
		catch(IOException e){
			e.printStackTrace();				
		}
		// achen:: end write to file
		
		
		 for (int i = 0; i < pixelList.size(); i++) 
		 { 
			 Pixel maxRhoTheta =  pixelList.get(i); int rh = maxRhoTheta.x; int degrees = maxRhoTheta.y;
			 //System.out.println("rho = " + rh); 
			 //System.out.println("degrees = " + degrees); 
			 finalGraphic.setColor(Color.RED); 
			 finalGraphic.fillRect(rh - 1, degrees - 1, 4, 4); 
			 }
		 

		System.out.println("Height of image:: " + finalImage.getHeight());
		System.out.println("Width of image:: " + finalImage.getWidth());

		File outputFile = new File(
				"C:\\Users\\achen\\Desktop\\Fall '19\\Master's Thesis\\Radon Transform Code\\Masters Thesis\\Masters Thesis\\Code\\BU Implementation version\\Test Results\\sofa_radon.png");
		try {
			ImageIO.write(finalImage, "png", outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * a private method which converts polar coordinates into cartesian coordinates
	 * 
	 * @param rho   - the coordinate's rho value, as an int
	 * @param theta - a double representing the coordinate's theta value
	 * @return a Pixel representing the corresponding cartesian coordinate
	 */
	private Pixel polarToCart(int rho, double theta) {
		int x = (int) Math.floor((double) rho * Math.cos(theta));
		int y = (int) Math.floor((double) rho * Math.sin(theta));
		Pixel point = new Pixel(x, y);
		return point;
	}

	/**
	 * a private method which converts a cartesian coordinate into a pixel on an
	 * image
	 * 
	 * @param x     - the x-value of the inputted coordinate
	 * @param y     - the y-value of the inputted coordinate
	 * @param edgeX - the width of the image
	 * @param edgeY - the height of the image
	 * @return a Pixel representing the coordinate's pixel on the image
	 */
	private Pixel cartToPixel(int x, int y, int edgeX, int edgeY) {
		int xPrime = 0;
		int yPrime = 0;
		if ((edgeX % 2) == 0) {
			if ((edgeY % 2) == 0) {
				int w = (edgeX / 2) - 1;
				int h = (edgeY / 2);
				xPrime = x + w;
				yPrime = (-y) + h;
			} else {
				int w = (edgeX / 2) - 1;
				int h = (edgeY + 1) / 2;
				xPrime = x + w;
				yPrime = (-y) + h;
			}
		} else if ((edgeY % 2) == 0) {
			int w = (edgeX - 1) / 2;
			int h = (edgeY / 2);
			xPrime = x + w;
			yPrime = (-y) + h;
		} else {
			int w = (edgeX - 1) / 2;
			int h = (edgeY + 1) / 2;
			xPrime = x + w;
			yPrime = (-y) + h;
		}
		Pixel point = new Pixel(xPrime, yPrime);
		return point;
	}

	/**
	 * a private helper method for makeLine which checks if any rho-theta values in
	 * the pixel list are too close to the inputted pixel
	 * 
	 * @param pixelList - the list of pixels to check
	 * @param pixel     - the inputted pixel
	 * @return false if no pixels are too close, true otherwise
	 */
	private boolean suppress(ArrayList<Pixel> pixelList, Pixel pixel) {
		boolean control = false;
		int rho = pixel.x;
		int theta = pixel.y;
		for (int i = 0; i < pixelList.size(); i++) {
			Pixel currentPixel = pixelList.get(i);
			int currRho = currentPixel.x;
			int currTheta = currentPixel.y;
			if (Math.abs(currRho - rho) <= 10) {
				if (Math.abs(currTheta - theta) <= 10) {
					control = true;
				} else {
					// leave control as false
				}
			} else {
				// leave control as false
			}
		}
		return control;
	}

	/**
	 * a private method that draws the edge line over the image
	 * 
	 * @param table - a double array representing all brightness values to each
	 *              corresponding rho, theta
	 * @return
	 */
	// need a better way to find multiple lines
	// maybe put all of the rho-theta combos and put them into a sorted list
	// then take the top however many
	// problem; how do you decide which edges to take?
	// need something that's not arbitrary
	private void makeLine(double[][] table, ArrayList<Pixel> pixelList) {

		/*
		 * ArrayList<Pixel> pixelList = new ArrayList<Pixel>();
		 * 
		 * int maxRho = table.length; int maxDegs = table[0].length; double percent =
		 * maxAveFinal * sensitivity;
		 * 
		 * // Original version using sensitivity
		 * 
		 * // takes out pixels within the threshold, and then removes those which are
		 * too // close together for (int outerRho = 0; outerRho < maxRho; outerRho++) {
		 * for (int outerDegs = 0; outerDegs < maxDegs; outerDegs++) { double currentVal
		 * = table[outerRho][outerDegs]; if (currentVal >= (maxAveFinal - percent)) {
		 * Pixel bigPixel = new Pixel(outerRho, outerDegs);
		 * 
		 * if (suppress(pixelList, bigPixel)) {
		 * 
		 * } else { pixelList.add(bigPixel); } } } }
		 * 
		 * 
		 * // Pure Non-max suppress for (int rho=2; rho<maxRho-2; rho++) { for (int
		 * deg=2; deg<maxDegs-2; deg++) { if ( table[rho][deg] > table[rho-1][deg-1] &&
		 * table[rho][deg] > table[rho-1][deg] && table[rho][deg] > table[rho-1][deg+1]
		 * && table[rho][deg] > table[rho][deg-1] && table[rho][deg] > table[rho][deg+1]
		 * && table[rho][deg] > table[rho+1][deg-1] && table[rho][deg] >
		 * table[rho+1][deg] && table[rho][deg] > table[rho+1][deg+1]) { Pixel bigPixel
		 * = new Pixel(rho, deg); pixelList.add(bigPixel); } } }
		 */

		// making the line from each point
		for (int j = 0; j < pixelList.size(); j++) {
			Pixel maxRhoTheta = pixelList.get(j);

			int rho = maxRhoTheta.x;
			int degrees = maxRhoTheta.y;

			int width = newImage.getWidth();
			int height = newImage.getHeight();

			double theta = (double) Math.toRadians(degrees);

			int n = (newImage.getWidth()) / 2;
			int r = (int) Math.floor(Math.sqrt((n * n) + (n * n)));

			Pixel cartPoint = polarToCart(rho, theta);
			Pixel pixelPoint = cartToPixel(cartPoint.x, cartPoint.y, width, height);

			Pixel pixelA = new Pixel(0, 0);
			Pixel pixelB = new Pixel(0, 0);

			int x = pixelPoint.x;
			int y = pixelPoint.y;

			// the way the point is projected out changes depending on which quadrant the
			// line sits in, so four different methods are required

			if ((theta >= 0) && (theta < Math.PI / 2)) {

				double theta1 = (double) Math.PI - (theta + Math.PI / 2);
				double thetaHat = (double) Math.PI / 2 - theta;
				double theta2 = (double) Math.PI - (thetaHat + Math.PI / 2);

				int a_x = x - (int) Math.floor((double) r * Math.cos(theta1));
				int a_y = y - (int) Math.floor((double) r * Math.sin(theta1));

				int b_x = x + (int) Math.floor((double) r * Math.sin(theta2));
				int b_y = y + (int) Math.floor((double) r * Math.cos(theta2));

				Pixel tempA = new Pixel(a_x, a_y);
				Pixel tempB = new Pixel(b_x, b_y);

				pixelA = tempA;
				pixelB = tempB;

			} else if ((theta >= Math.PI / 2) && (theta < Math.PI)) {

				double thetaT = (double) theta - Math.PI / 2;
				double theta1 = (double) Math.PI - (thetaT + Math.PI / 2);
				double thetaHat = (double) (Math.PI / 2) - thetaT;
				double theta2 = (double) Math.PI - (thetaHat + Math.PI / 2);

				int a_x = x - (int) Math.floor((double) r * Math.sin(theta1));
				int a_y = y + (int) Math.floor((double) r * Math.cos(theta1));

				int b_x = x + (int) Math.floor((double) r * Math.cos(theta2));
				int b_y = y - (int) Math.floor((double) r * Math.sin(theta2));

				Pixel tempA = new Pixel(a_x, a_y);
				Pixel tempB = new Pixel(b_x, b_y);

				pixelA = tempA;
				pixelB = tempB;

			} else if ((theta >= Math.PI) && (theta < 3 * Math.PI / 2)) {

				double thetaT = (double) theta - Math.PI;
				double thetaHat = (double) (Math.PI / 2) - thetaT;
				double theta1 = (double) Math.PI - (thetaHat + Math.PI / 2);
				double theta2 = (double) Math.PI - (thetaT + Math.PI / 2);

				int a_x = x - (int) Math.floor((double) r * Math.sin(theta1));
				int a_y = y - (int) Math.floor((double) r * Math.cos(theta1));

				int b_x = x + (int) Math.floor((double) r * Math.cos(theta2));
				int b_y = y + (int) Math.floor((double) r * Math.sin(theta2));

				Pixel tempA = new Pixel(a_x, a_y);
				Pixel tempB = new Pixel(b_x, b_y);

				pixelA = tempA;
				pixelB = tempB;

			} else {

				double thetaT = (double) theta - 3 * (Math.PI / 2);
				double thetaHat = (double) (Math.PI / 2) - thetaT;
				double theta1 = (double) Math.PI - (thetaHat + Math.PI / 2);
				double theta2 = (double) Math.PI - (thetaT + Math.PI / 2);

				int a_x = x - (int) Math.floor((double) r * Math.cos(theta1));
				int a_y = y + (int) Math.floor((double) r * Math.sin(theta1));

				int b_x = x + (int) Math.floor((double) r * Math.sin(theta2));
				int b_y = y - (int) Math.floor((double) r * Math.cos(theta2));

				Pixel tempA = new Pixel(a_x, a_y);
				Pixel tempB = new Pixel(b_x, b_y);

				pixelA = tempA;
				pixelB = tempB;

			}

			PerpLine perpLine = new PerpLine();
			ArrayList<Pixel> pixelListFinal = perpLine.makePoints(pixelA, pixelB);

			for (int i = 0; i < pixelListFinal.size(); i++) {
				Pixel currentPixel = pixelListFinal.get(i);
				graphic.setColor(Color.GREEN);
				graphic.fillRect(currentPixel.x, currentPixel.y, 1, 1);
			}

			graphic.setColor(Color.RED);
			graphic.fillRect(x, y, 5, 5);
		}

		// this saves the completed image to a file, taking it out doesn't affect
		// anything
		File outputFile = new File(
				"C:\\Users\\achen\\Desktop\\Fall '19\\Master's Thesis\\Radon Transform Code\\Masters Thesis\\Masters Thesis\\Code\\BU Implementation version\\Radon\\images\\sofa_saved.jpg");
		try {
			ImageIO.write(image, "jpg", outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// return pixellist
		//return pixelList;

	}

	/**
	 * a method which hands in the image on the window
	 */
	public void paintComponent(Graphics g) {
		// switch out the first variable for whichever image you want to display
//		g.drawImage(finalImage, 0, 0, getWidth(), getHeight(), null);
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
	}

	/**
	 * a private method that finds the radon transform of an image
	 * 
	 * @param image      - a BufferedImage to be examined
	 * @param maxRho     - the maximum rho value of the image
	 * @param maxDegrees - the maximum degrees to check
	 * @return a 2D array of doubles which corresponds each brightness value to a
	 *         rho, theta
	 */
	private double[][] radon(BufferedImage image, int maxRho, int maxDegrees) {
		int N = image.getWidth();

		int numLevels = (int) ((double) Math.log(N) / Math.log(2)) + 1;
		int shiftMax = (int) Math.pow(2, numLevels - 1);
		int offsetMax = N + shiftMax;

		double[][] table_sum = new double[maxRho][maxDegrees];
		double[][] table_count = new double[maxRho][maxDegrees];

		// Arrays for p and q (right and up of the original image
		// in the coordinate system of the rotated / flipped image)
		double P_x[] = { 1, 0, -1, 0, 0, 1, 0, -1 };
		double P_y[] = { 0, -1, 0, 1, 1, 0, -1, 0 };

		double Q_x[] = { 0, 1, 0, -1, 1, 0, -1, 0 };
		double Q_y[] = { 1, 0, -1, 0, 0, -1, 0, 1 };

		BufferedImage img2 = image;

		// performs the radon transform 8 times over the flipped and rotated images
		for (int i = 0; i < 8; i++) {

			// perform radon transform
			FastRadon radon = new FastRadon(img2);
			HashMap<Integer, int[][][]> radon1 = radon.findLine();
			int[][][] table1 = radon1.get(numLevels - 1);

			// bin shift and offset values into rho and theta values
			for (int S = 0; S < shiftMax; S++) {
				for (int F = 0; F < offsetMax; F++) {
					// calculated the normalized u and v vectors
					double u_x = -S;
					double u_y = (N - 1.0);
					double inv_uLen = 1.0 / Math.sqrt((u_x * u_x) + (u_y * u_y));
					u_x *= inv_uLen;
					u_y *= inv_uLen;
					double v_x = u_y;
					double v_y = -u_x;

					double p_x = F;
					double p_y = 0;
					double c_x = (N - 1.0) / 2.0;
					double c_y = (N - 1.0) / 2.0;

					// calculate rho
					double rhoTop = (double) ((c_x * u_y) - (c_y * u_x) - (p_x * u_y) + (p_y * u_x));
					double rho = (double) rhoTop / ((v_x * u_y) - (v_y * u_x));

					// Invert v, and rotate / flip using p and q
					v_x = -v_x; // v points away from the center now
					v_y = -v_y;
					double w_x = (v_x * P_x[i]) + (v_y * P_y[i]);
					double w_y = (v_x * Q_x[i]) + (v_y * Q_y[i]);

					// calculate theta
					double theta = Math.atan2(w_y, w_x);
					if (theta < 0) {
						theta += ((double) 2.0 * Math.PI);
					}

					// if rho is less than one, turn theta around 180 degrees and make rho positive
					if (rho < 0) {
						rho = Math.abs(rho);
						theta += Math.PI;
					}

					// bin by rho and theta
					int iRho = (int) Math.floor(rho);
					int iTheta = (int) Math.floor(Math.toDegrees(theta));

					if (iRho < 0) {
						iRho = 0;
					}

					if (iRho > maxRho - 1) {
						iRho = maxRho - 1;
					}

					if (iTheta < 0) {
						iTheta += maxDegrees;
					}

					if (iTheta > maxDegrees - 1) {
						iTheta = iTheta - maxDegrees;
					}

					table_sum[iRho][iTheta] += table1[0][S][F];
					table_count[iRho][iTheta]++;
				}
			}

			// rotate or mirror the image
			if (i == 3) {
				Morph r = new Morph(img2);
				Color[][] colorTable = r.mirror();
				img2 = configu.createCompatibleImage(N, N);
				Graphics2D graphicR4 = img2.createGraphics();
				for (int x = 0; x < N; x++) {
					for (int y = 0; y < N; y++) {
						Color color = colorTable[x][y];
						graphicR4.setColor(color);
						graphicR4.fillRect(x, y, 1, 1);
					}
				}
			} else if (i < 7) {
				Morph r = new Morph(img2);
				Color[][] colorTable = r.rotate(Math.PI / 2);
				img2 = configu.createCompatibleImage(N, N);
				Graphics2D graphicR4 = img2.createGraphics();
				for (int x = 0; x < N; x++) {
					for (int y = 0; y < N; y++) {
						Color color = colorTable[x][y];
						graphicR4.setColor(color);
						graphicR4.fillRect(x, y, 1, 1);
					}

				}
			}
		}

		double maxAve = 0;

		// averages out every value between count and total, and finds the max average
		// value
		double table[][] = new double[maxRho][maxDegrees];
		for (int rho = 0; rho < maxRho; rho++) {
			for (int theta = 0; theta < maxDegrees; theta++) {
				if (table_count[rho][theta] > 0) {
					table[rho][theta] = table_sum[rho][theta] / table_count[rho][theta];
					if (table[rho][theta] >= maxAve) {
						maxAve = table[rho][theta];
					}
				} else
					table[rho][theta] = 0;
			}

		}
		maxAveFinal = maxAve;
		return table;
	}

}
