package testPackage;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Morph {
	public BufferedImage image;

	/**
	 * a constructor for the Morph class
	 * 
	 * @param image - a BufferedImage to be morphed
	 */
	public Morph(BufferedImage image) {
		this.image = image;
	}

	/**
	 * a method which mirrors an image over the vertical center line
	 * 
	 * @return a 2D array of Colors corresponding to the mirrored image
	 */
	public Color[][] mirror() {
		int width = image.getWidth();
		int height = image.getHeight();
		Color[][] table = new Color[width][height];
		double centerX = ((double) (width - 1)) / 2.0;
		// y stays the same, x reflects over middle line
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int newX = 0;
				if (x > centerX) {
					int difference = (int) Math.floor(x - centerX);
					newX = (int) Math.floor(centerX - difference);
				} else {
					int difference = (int) Math.floor(centerX - x);
					newX = (int) Math.floor(centerX + difference);
				}
				int clr = image.getRGB(x, y);
				int red = (clr & 0x00ff0000) >> 16;
				int green = (clr & 0x0000ff00) >> 8;
				int blue = clr & 0x000000ff;
				Color color = new Color(red, green, blue);
				table[newX][y] = color;
			}
		}
		return table;
	}

	/**
	 * a method which rotates an image clockwise by some angle
	 * 
	 * @param angle - the angle, in radians, which you want to rotate. must be a
	 *              multiple of PI/2
	 * @return a 2D array of Colors corresponding to the rotated image
	 */
	public Color[][] rotate(double angle) {
		int width = image.getWidth();
		int height = image.getHeight();
		Color[][] table = new Color[width][height];
		double centerX = ((double) (width - 1)) / 2.0;
		double centerY = ((double) (height - 1)) / 2.0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int clr = image.getRGB(x, y);
				int red = (clr & 0x00ff0000) >> 16;
				int green = (clr & 0x0000ff00) >> 8;
				int blue = clr & 0x000000ff;
				Color color = new Color(red, green, blue);
				double dx = ((double) x) - centerX;
				double dy = ((double) y) - centerY;
				double newX = Math.cos(angle) * dx - Math.sin(angle) * dy + centerX;
				double newY = Math.cos(angle) * dy + Math.sin(angle) * dx + centerY;
				int ix = (int) Math.round(newX);
				int iy = (int) Math.round(newY);
				table[ix][iy] = color;
			}
		}
		return table;
	}

	/**
	 * a private helper method for findDerivative
	 * 
	 * @return a double array corresponding the to x-matrix values
	 */
	private double[][] firstOfX() {
		// | -1 0 1 |
		// | -2 0 2 |
		// | -1 0 1 |
		int width = image.getWidth();
		int height = image.getHeight();
		double[][] result = new double[width - 2][height - 2];
		for (int x = 1; x < width - 2; x++) {
			for (int y = 1; y < height - 2; y++) {
				Pixel oneOne = new Pixel(x - 1, y - 1);
				Pixel oneThree = new Pixel(x + 1, y - 1);
				Pixel twoOne = new Pixel(x - 1, y);
				Pixel twoThree = new Pixel(x + 1, y);
				Pixel threeOne = new Pixel(x - 1, y + 1);
				Pixel threeThree = new Pixel(x + 1, y + 1);
				double oneOneValue = oneOne.giveBrightness(image);
				double oneThreeValue = oneThree.giveBrightness(image);
				double twoOneValue = twoOne.giveBrightness(image);
				double twoThreeValue = twoThree.giveBrightness(image);
				double threeOneValue = threeOne.giveBrightness(image);
				double threeThreeValue = threeThree.giveBrightness(image);
				double finalValue = (double) ((double) -1 * oneOneValue) + ((double) 1 * oneThreeValue)
						+ ((double) -2 * twoOneValue) + ((double) 2 * twoThreeValue) + ((double) -1 * threeOneValue)
						+ ((double) 1 * threeThreeValue);
				result[x - 1][y - 1] = finalValue;
			}
		}
		return result;
	}

	/**
	 * a private helper method for findDerivative
	 * 
	 * @return a double array corresponding the to y-matrix values
	 */
	private double[][] firstOfY() {
		// | -1 -2 -1 |
		// | 0 0 0 |
		// | 1 2 1 |
		int width = image.getWidth();
		int height = image.getHeight();
		double[][] result = new double[width - 2][height - 2];
		for (int x = 1; x < width - 2; x++) {
			for (int y = 1; y < height - 2; y++) {
				Pixel oneOne = new Pixel(x - 1, y - 1);
				Pixel oneTwo = new Pixel(x, y - 1);
				Pixel oneThree = new Pixel(x + 1, y - 1);
				Pixel threeOne = new Pixel(x - 1, y + 1);
				Pixel threeTwo = new Pixel(x, y + 1);
				Pixel threeThree = new Pixel(x + 1, y + 1);
				double oneOneValue = oneOne.giveBrightness(image);
				double oneTwoValue = oneTwo.giveBrightness(image);
				double oneThreeValue = oneThree.giveBrightness(image);
				double threeOneValue = threeOne.giveBrightness(image);
				double threeTwoValue = threeTwo.giveBrightness(image);
				double threeThreeValue = threeThree.giveBrightness(image);
				double finalValue = (double) ((double) -1 * oneOneValue) + ((double) -2 * oneTwoValue)
						+ ((double) -1 * oneThreeValue) + ((double) 1 * threeOneValue) + ((double) 2 * threeTwoValue)
						+ ((double) 1 * threeThreeValue);
				result[x - 1][y - 1] = finalValue;
			}
		}
		return result;
	}

	/**
	 * a method which finds the derivative of an image
	 * 
	 * @return a 2D array of doubles which corresponds to the RGB values of the
	 *         derived image
	 */
	public double[][] findDerivative() {
		// using Sobel operator
		int width = image.getWidth();
		int height = image.getHeight();
		double[][] xDerivative = firstOfX();
		double[][] yDerivative = firstOfY();
		double[][] finalImageArray = new double[width - 2][height - 2];
		for (int x = 0; x < width - 2; x++) {
			for (int y = 0; y < height - 2; y++) {
				double fOfX = xDerivative[x][y];
				double fOfY = yDerivative[x][y];
				double toSqrt = (fOfX * fOfX) + (fOfY * fOfY);
				double finalMagnitude = Math.sqrt(toSqrt);
				finalImageArray[x][y] = finalMagnitude;
			}
		}
		return finalImageArray;
	}

	/**
	 * a method which maximally erodes an image
	 * 
	 * @return a 2D array of doubles which corresponds to the RGB values of the
	 *         eroded image
	 */
	public double[][] erode() {
		int width = image.getWidth();
		int height = image.getHeight();
		double[][] valueArray = new double[width - 2][height - 2];
		// doing 3x3 max kernel
		for (int x = 1; x < width - 2; x++) {
			for (int y = 1; y < height - 2; y++) {
				Pixel oneOne = new Pixel(x - 1, y - 1);
				Pixel oneTwo = new Pixel(x, y - 1);
				Pixel oneThree = new Pixel(x + 1, y - 1);
				Pixel twoOne = new Pixel(x - 1, y);
				Pixel mid = new Pixel(x, y);
				Pixel twoThree = new Pixel(x + 1, y);
				Pixel threeOne = new Pixel(x - 1, y + 1);
				Pixel threeTwo = new Pixel(x, y + 1);
				Pixel threeThree = new Pixel(x + 1, y + 1);
				double oneOneValue = oneOne.giveBrightness(image);
				double oneTwoValue = oneTwo.giveBrightness(image);
				double oneThreeValue = oneThree.giveBrightness(image);
				double twoOneValue = twoOne.giveBrightness(image);
				double midValue = mid.giveBrightness(image);
				double twoThreeValue = twoThree.giveBrightness(image);
				double threeOneValue = threeOne.giveBrightness(image);
				double threeTwoValue = threeTwo.giveBrightness(image);
				double threeThreeValue = threeThree.giveBrightness(image);
				double maxValueOne = Math.max(oneOneValue, Math.max(oneTwoValue, oneThreeValue));
				double maxValueTwo = Math.max(twoOneValue, Math.max(midValue, twoThreeValue));
				double maxValueThree = Math.max(threeOneValue, Math.max(threeTwoValue, threeThreeValue));
				double finalMax = Math.max(maxValueOne, Math.min(maxValueTwo, maxValueThree));
				valueArray[x - 1][y - 1] = finalMax;
			}
		}
		return valueArray;
	}

}
