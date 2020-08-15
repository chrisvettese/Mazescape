package chris.mazescape;

/**A rectangle collision file I created since java's rectangle class doesn't exist on android*/
public class Rectangle {
	public float x, y, xwidth, yheight;
	//Sets rectangle based on x, y,  width and height
	public void set(float x, float y, int width, int height) {
		this.x = x;
		this.y = y;
		this.xwidth = x + width;
		this.yheight = y + height;
	}
	//Sets the rectangle given the location of 2 corners of it. Useful for rotated sprites
	public void set(float x1, float y1, float x3, float y3) {
		if (x3 < x1) {
			x = x3;
			xwidth = x1;
		} else {
			x = x1;
			xwidth = x3;
		}
		if (y3 < y1) {
			y = y3;
			yheight = y1;
		} else {
			y = y1;
			yheight = y3;
		}
	}
	//Checks if 2 rectangles overlap
	public static boolean intersects(Rectangle r1, Rectangle r2) {
		return r1.x <= r2.xwidth && r1.xwidth >= r2.x && r1.y <= r2.yheight && r1.yheight >= r2.y;
	}
	//Check if a point is inside this rectangle
	public boolean contains(float x, float y) {
		return x > this.x && x < xwidth && y > this.y && y < yheight;
	}
}