package testPackage;

import java.awt.image.BufferedImage;

public class Pixel {
	public int x;
	public int y;

	/**
	 * a constructor for the Pixel class
	 * 
	 * @param x - the x-coordinate of the pixel
	 * @param y - the y-coordinate of the pixel
	 */
	public Pixel(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * a method that gives the brightness value of a pixel in an image
	 * 
	 * @param image - a BufferedImage which contains the pixel
	 * @return a double representing the brightness value of the pixel in the image
	 */
	public double giveBrightness(BufferedImage image) {
		int x = this.x;
		int y = this.y;
		int rgb = image.getRGB(x, y);
		int red = (rgb >> 16) & 0x000000FF;
		int green = (rgb >> 8) & 0x000000FF;
		int blue = (rgb) & 0x000000FF;
		double luminance = ((red * 0.299) + (green * 0.587) + (blue * 0.114));
		return luminance;
	}

}
