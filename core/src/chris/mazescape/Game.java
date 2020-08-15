package chris.mazescape;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import chris.mazescape.gameobjects.GameObject;

import java.util.ArrayList;
import java.util.List;

/**This game state is for the actual game, when the player has to complete a mission*/
public class Game extends GameState {
	//Array of the walls of the maze
	public boolean[][] maze;
	public Generation generation;
	public static GameObject object = new GameObject();
	public Controls control;
	public Draw draw;
	public ItemScreen item;
	//Size in pixels of one wall
	public static final byte size = 20;
	
	public Color mazeColour, background;
	
	public Mission mission;
	public Button[] buttons;
	//Indexes of each button
	public static final int MOVE = 0, MENU = 1, ITEMS = 2;
	
	//At the end of each loop, delete the objects in this list
	private List<Integer> deleteList;
	private int level;
	public int score;
	
	public Game() {
		draw = new Draw();
		buttons = new Button[3];
		buttons[0] = new Button(Main.info.width - 29 / Main.camera.zoom, Main.info.height - 29 / Main.camera.zoom, Button.CIRCLE, Button.ONE_CLICK, 11, 11, new Color(145/255f, 145/255f, 145/255f, 190/255f), new Color(90/255f, 90/255f, 90/255f, 190/255f), null);
		buttons[1] = new Button((12 - 10 / 2f) / Main.camera.zoom, 12 / Main.camera.zoom, Button.RECT, Button.ONE_CLICK, 10, 10, buttons[MOVE].normal, buttons[MOVE].pressed, new Sprite(new Texture("menu.png")));
		buttons[2] = new Button(draw.timeCoordinates[Generation.X] + 8 / Main.camera.zoom, draw.timeCoordinates[Generation.Y] + 28 / Main.camera.zoom, Button.RECT, Button.ON_OFF, 16, 13, buttons[MOVE].normal, buttons[MOVE].pressed, new Sprite(new Texture("button_stick.png")));
	}
	public void setLevel(int level) {
		object = new GameObject();
		object.load();
		this.level = level;
		mission = new Mission(level);
		initialize();
	}
	public void setLevel(List<String> info) {
		object = new GameObject();
		object.load();
		System.out.println("1:  "+info);
		level = Integer.parseInt(info.get(0));
		Main.state.transition.setNextLevel(level);
		info.remove(0);
		mission = new Mission(info);
		initialize();
	}
	private void initialize() {
		deleteList = new ArrayList<>();
		
		generation = new Generation();
		control = new Controls();
		item = new ItemScreen();
		mazeColour = new Color();
		background = new Color(100/255f, 100/255f, 100/255f, 1);
		
		//Makes random wall colour
		while (mazeColour.r <= 0.78 && mazeColour.g <= 0.78 && mazeColour.b <= 0.78) {
			mazeColour.r = (float) Math.random();
			mazeColour.g = (float) Math.random();
			mazeColour.b = (float) Math.random();
		}
		//Generates a maze
		maze = Generation.generateMaze();
		//Spreads items and enemies around the maze
		generation.addStartingObjects();
		//Starts pathfinding
		generation.manager.startThread();
	}
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		//Respawns enemies if necessary
		generation.spawnEnemies();
		if (!buttons[ITEMS].isPressed()) {
			buttons[MOVE].isPressed();
			if (buttons[MENU].isPressed()) {
				Main.state.menu.saveScreen = false;
				Main.state.menu.saveGame();
				stopGame(Main.state.menu);
				return;
			}
		}
		int playerIndex = generation.types.indexOf(GameObject.player.index);
		float px = generation.coordinates.get(playerIndex)[Generation.X] / size;
		float py = generation.coordinates.get(playerIndex)[Generation.Y] / size;
		draw.draw(batch, renderer, px, py, playerIndex);
		//If player has won the game
		if (mission.verify()) {
			stopGame(Main.state.transition);
			return;
		}
		//Game is lost because player ran out of time
		if (mission.timer == Mission.TIMER_LOSE && mission.time <= 0) {
			Main.state.transition.levelLost = true;
			stopGame(Main.state.transition);
			return;
		}
		for (int i = 0; i < deleteList.size(); i++) {
			if (generation.types.get(generation.uIndexList.indexOf(deleteList.get(i))) == GameObject.player.index) {
				Main.state.transition.levelLost = true;
				Main.state.menu.saveGame();
				stopGame(Main.state.transition);
				return;
			}
			generation.remove(deleteList.get(i));
		}
		deleteList.clear();
	}
	/**
	 * Adds index to the delete list
	 * @param uIndex
	 */
	public void delete(int uIndex) {
		deleteList.add(uIndex);
	}
	/**
	 * Stops the current mission, refreshes the game object. If the new gamestate is transition,
	 * then the player has won and the level increases
	 * @param state
	 */
	public void stopGame(GameState state) {
		if (state == Main.state.transition) Main.state.transition.setNextLevel(level + 1);
		Main.state.gamestate = state;
		dispose();
		Main.state.game = new Game();
	}
	/**
	 * Ends the enemy pathfinding thread, disposes of textures and sounds
	 */
	@Override
	public void dispose() {
		buttons[1].dispose();
		buttons[2].dispose();
		object.dispose();
		if (control != null) {
			control.dispose();
		}
		if (generation != null) {
			generation.manager.stopThread();
		}
	}
}