package chris.mazescape;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**A simple class for managing the different parts of the game. Menu is where you select a save, Transition is
 * where the new mission is shown and is the win/losing screen. Game is where you try to complete the mission
 * and avoid dying*/
public class GameState {
	public Menu menu;
	public Game game;
	public Transition transition;
	//The current state, assigned to one of the above game states
	public GameState gamestate;
	//Loads the game states, sets the current state to menu
	public void load() {
		game = new Game();
		menu = new Menu();
		transition = new Transition();
		gamestate = menu;
	}
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		gamestate.draw(batch, renderer);
	}
	public void dispose() {
		game.dispose();
		menu.dispose();
		transition.dispose();
	}
}