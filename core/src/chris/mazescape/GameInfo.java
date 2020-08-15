package chris.mazescape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

/**
 * A class for getting game information like screen width, height, and camera zoom. Also has pointers to methods
 * in the library that allow the game to work on android
 */
public class GameInfo {
	//screen width and height
	public int width, height;
	//real zoom of game
	public float zoom;
	//Adjustable zoom of game. Higher values = less zoomed in
	private final int scale = 207;
	
	public GameInfo() {
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		//Calculates real zoom based on phone screen resolution
		zoom = scale / ((width + height) / 2.0f);
	}
	/**
	 * Converts from screen coordinates, stored in a Vector3 object (contains x, y, and z, but z is not used)
	 * Equivalent to having a cameraX and cameraY variable, subtracting the Vector3 from the camera position, and
	 * multiplying by the zoom. The camera included in the library works better for android, since it is much more efficient
	 * (no subtracting every time you render something)
	 */
	public Vector3 getGameCoordinates(Vector3 point) {
		return Main.camera.unproject(point);
	}
	/**
	 * Converts from game coordinates back to screen
	 * Equivalent to subtracting cameraX and cameraY again, and dividing by the zoom, but more efficient
	 */
	public Vector3 getScreenCoordinates(Vector3 point) {
		return Main.camera.project(point);
	}
	/**
	 * @return On computers, the coordinates of the mouse. On phones, the coordinates of touch. Similar to
	 * mouseX and mouseY in processing, but this works for phones and computers
	 */
	public float getX() {
		return Gdx.input.getX();
	}
	public float getY() {
		return Gdx.input.getY();
	}
	/**
	 * @return On phones you can touch the screen in more than one place, so this returns the coordinates of a
	 * specific point of contact. You can have a loop between 1 and 20, check if each one is touching the screen,
	 * and if it is, then check the location (getX and getY)
	 */
	public float getX(int num) {
		return Gdx.input.getX(num);
	}
	public float getY(int num) {
		return Gdx.input.getY(num);
	}
	/**
	 * @return Whether or not the screen is getting clicked or touched
	 */
	public boolean isTouched() {
		return Gdx.input.isTouched();
	}
	/**
	 * The time in seconds from the last frame. Equivalent to subtracting each 
	 * System.currentTimeMillis() from the previous and dividing by 1000
	 */
	public float getDeltaTime() {
		return Gdx.graphics.getDeltaTime();
	}
}