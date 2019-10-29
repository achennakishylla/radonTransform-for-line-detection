package testPackage;

import java.awt.image.BufferedImage;
import java.util.HashMap;

// this assumes a square N x N image where N is a power of 2
public class FastRadon {
	public BufferedImage image;

	/**
	 * a constructor for the FastRadon class
	 * 
	 * @param image - a BufferedImage to be radoned
	 */
	public FastRadon(BufferedImage image) {
		this.image = image;
	}

	/**
	 * a private method that constructs the HashMap to hold all the level to slice,
	 * shift, offset values
	 * 
	 * @param n - an int representing the width of the image
	 * @return a HashMap that maps the level to a 3D double array of slice, shift,
	 *         offset
	 */
	private HashMap<Integer, int[][][]> newTable(int n) {
		HashMap<Integer, int[][][]> map = new HashMap<>();
		int numLevels = (int) (Math.log(n) / Math.log(2)) + 1;
		for (int level = 0; level < numLevels; level++) {
			int numSlices = (int) Math.ceil((double) n / (Math.pow(2, level)));
			int numShifts = (int) Math.pow(2, level);
			int maxOffsets = n + (int) Math.pow(2, level + 1);
			int[][][] table = new int[numSlices][numShifts][maxOffsets];
			map.put(level, table);
		}
		return map;
	}

	/**
	 * a method which finds the radon transform of an image from 90 to 135 degrees
	 * 
	 * @return a HashMap that maps the level to a 3D double array of slice, shift,
	 *         offset, which holds the radon values
	 */
	public HashMap<Integer, int[][][]> findLine() {
		int n = image.getWidth(); // Np
		HashMap<Integer, int[][][]> table = newTable(n);
		int numLevels = (int) (Math.log(n) / Math.log(2)) + 1; // Nl
		for (int level = 0; level < numLevels; level++) {
			int numSlices = (n) / ((int) Math.pow(2, level)); // Nc[L]
			int sliceHeight = (int) Math.pow(2, level); // Nh[L]
			for (int slice = 0; slice < numSlices; slice++) {
				int numShifts = sliceHeight;
				for (int shift = 0; shift < numShifts; shift++) {
					int numOffsets = n + shift;
					for (int offset = 0; offset < numOffsets; offset++) {
						if (level == 0) {
							Pixel pixel = new Pixel(offset, (n - 1) - slice);
							int brightness = (int) Math.floor(pixel.giveBrightness(image));
							int[][][] tempTable = table.get(level);
							tempTable[slice][shift][offset] = brightness;
							table.put(level, tempTable);
						} else {
							int fL = offset;
							int fU = Math.abs(offset - (int) Math.ceil((double) shift / 2));
							int floorsOver2 = (int) Math.floor((double) shift / 2);
							int[][][] tempTable = table.get(level);
							int[][][] valTable = table.get(level - 1);
							int firstVal = valTable[2 * slice][floorsOver2][fL];
							int secondVal = valTable[(2 * slice) + 1][floorsOver2][fU];
							tempTable[slice][shift][offset] = firstVal + secondVal;
							table.put(level, tempTable);
						}
					}
				}
			}
		}
		return table;
	}

}
