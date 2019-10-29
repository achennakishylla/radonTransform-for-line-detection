package testPackage;

import java.util.ArrayList;

public class PerpLine {

	/**
	 * a constructor for the PerpLine class
	 */
	public PerpLine() {
	}

	/**
	 * a method that constructs a straight line of pixels between two points
	 * 
	 * @param a - a pixel for one point of the line
	 * @param b - a pixel for another point of the line
	 * @return an ArrayList of pixels corresponding to each pixel on the completed
	 *         line
	 */
	public ArrayList<Pixel> makePoints(Pixel a, Pixel b) {
		ArrayList<Pixel> list = new ArrayList<Pixel>();
		int x1 = a.x;
		int y1 = a.y;
		int x2 = b.x;
		int y2 = b.y;
		int w = x2 - x1;
		int h = y2 - y1;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
		if (w < 0)
			dx1 = -1;
		else if (w > 0)
			dx1 = 1;
		if (h < 0)
			dy1 = -1;
		else if (h > 0)
			dy1 = 1;
		if (w < 0)
			dx2 = -1;
		else if (w > 0)
			dx2 = 1;
		int longest = Math.abs(w);
		int shortest = Math.abs(h);
		if (!(longest > shortest)) {
			longest = Math.abs(h);
			shortest = Math.abs(w);
			if (h < 0)
				dy2 = -1;
			else if (h > 0)
				dy2 = 1;
			dx2 = 0;
		}
		int numerator = longest >> 1;
		for (int i = 0; i <= longest; i++) {
			Pixel point = new Pixel(x1, y1);
			list.add(point);
			numerator += shortest;
			if (!(numerator < longest)) {
				numerator -= longest;
				x1 += dx1;
				y1 += dy1;
			} else {
				x1 += dx2;
				y1 += dy2;
			}
		}
		return list;
	}

}
