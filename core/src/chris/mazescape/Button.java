package chris.mazescape;

//Classes from the library
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

/**
 * A way to easily add buttons in the game. They can either be rectangular or circular, and be on/off buttons or 
 * buttons that are only on while being clicked
 */
public class Button {
	public static final int RECT = 0, CIRCLE = 1;
	public static final int ONE_CLICK = 0, ON_OFF = 1;
	
	public boolean screenTouched, buttonContains, pressButton;
	public int shape, width, height;
	public Color normal, pressed;
	public float x, y;
	public int type, pointer;
	//Used for collision with the button
	private Rectangle rect;
	private boolean buttonPressed;
	//Vector3 is used for converting between the screen coordinates and in game coordinates
	private Vector3 point;
	public Vector3 buttonLocation;
	//This is used for on/off buttons. Every time buttonPressed is true, buttonState switches
	public boolean buttonState;
	//If true, button location is calculated. If false, button location is provided coordinates
	public boolean useCamera;
	public Sprite image;
	
	public Button(float x, float y, int shape, int type, int width, int height, Color normal, Color pressed, Sprite image) {
		useCamera = true;
		point = new Vector3();
		buttonLocation = new Vector3();
		buttonState = false;
		this.image = image;
		this.shape = shape;
		this.width = width;
		this.height = height;
		this.normal = normal;
		this.pressed = pressed;
		if (shape == RECT) this.x = x - width / 2 / Main.camera.zoom;
		else this.x = x;
		this.y = y;
		this.type = type;
		if (shape == RECT) {
			rect = new Rectangle();
			rect.set(x, y, width, height);
		}
		Main.info.getGameCoordinates(point.set(100, 100, 0));
		Main.info.getGameCoordinates(buttonLocation.set(x, y, 0));
	}
	public void set(float x, float y) {
		buttonLocation.x = x;
		buttonLocation.y = y;
	}
	/**
	 * Lots of confusing variables. A lot of this was guess and check. Returns if the button is pressed, as in clicked or tapped
	 * @return
	 */
	public boolean isPressed() {
		if (useCamera) Main.info.getGameCoordinates(buttonLocation.set(x, y, 0));
		buttonPressed = false;
		if (Main.info.isTouched()) {
			Main.info.getGameCoordinates(point.set(Main.info.getX(), Main.info.getY(), 0));
			if (shape == RECT) {
				rect.set(buttonLocation.x, buttonLocation.y, width, height);
				if (rect.contains(point.x, point.y)) {
					buttonContains = true;
					int i = 0;
					while (Main.info.getX(i) != Main.info.getX() && Main.info.getY(i) != Main.info.getY() && i < 10) {
						i += 1;
					}
					pointer = i;
 				} else buttonContains = false;
			} else {
				if (Math.sqrt(Math.pow(buttonLocation.x - point.x, 2) + Math.pow(buttonLocation.y - point.y, 2)) < width) buttonContains = true;
				else buttonContains = false;
			}
			if (!buttonContains && !screenTouched) {
				pressButton = false;
			} else {
				if (!screenTouched && buttonContains) pressButton = true;
			}
			screenTouched = true;
			return false;
		} else {
			if (buttonContains && screenTouched && pressButton) {
				buttonPressed = true;
			}
			buttonContains = false;
			screenTouched = false;
			pressButton = false;
			if (buttonPressed && type == ON_OFF) buttonState ^= true;
			if (type == ON_OFF) return buttonState;
			return buttonPressed;
		}
	}
	/**
	 * Call this before draw. For efficiency, render all the buttons on a screen before drawing them
	 * @param renderer
	 */
	public void render(ShapeRenderer renderer) {
		if ((type == ONE_CLICK && pressButton) || (type == ON_OFF && buttonState) || (type == ON_OFF && !buttonState && pressButton)) renderer.setColor(pressed);
		else renderer.setColor(normal);
		if (shape == RECT) {
			renderer.rect(buttonLocation.x, buttonLocation.y, width, height);
		} else {
			renderer.circle(buttonLocation.x, buttonLocation.y, width, 30);
		}
	}
	/**
	 * If button has an image instead of text, draw the image
	 * @param batch
	 */
	public void draw(SpriteBatch batch) {
		if (image != null) {
			image.setPosition(buttonLocation.x, buttonLocation.y);
			image.draw(batch);
		}
	}
	/**
	 * Dispose of the image
	 */
	public void dispose() {
		image.getTexture().dispose();
	}
}