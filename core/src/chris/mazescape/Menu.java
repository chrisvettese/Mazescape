package chris.mazescape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**Where the game starts, and where the player selects a save*/
public class Menu extends GameState {
	private final String[] names = {"Start Game", "Save 1", "Save 2", "Save 3", "Save 4", "Save 5"};
	private Vector3[] text;
	//Start button and save buttons
	private Button[] buttons;
	//If the saves should be displayed
	public boolean saveScreen;
	private byte save;
	
	public Menu() {
		text = new Vector3[] {new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3()};
		buttons = new Button[6];
		buttons[0] = new Button(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2 - 30 / Main.camera.zoom, Button.RECT, Button.ONE_CLICK, 100, 20, new Color(90/255f, 160/255f, 160/255f, 1), new Color(45/255f, 80/255f, 80/255f, 1), null);
		buttons[4] = new Button(Gdx.graphics.getWidth() - 36 / Main.camera.zoom, Gdx.graphics.getHeight() / 2 + 20 / Main.camera.zoom, Button.RECT, Button.ONE_CLICK, 65, 20, new Color(90/255f, 160/255f, 90/255f, 1), new Color(45/255f, 80/255f, 45/255f, 1), null);
		buttons[2] = new Button(Gdx.graphics.getWidth() - 36 / Main.camera.zoom, Gdx.graphics.getHeight() / 2 - 20 / Main.camera.zoom, Button.RECT, Button.ONE_CLICK, 65, 20, new Color(90/255f, 160/255f, 90/255f, 1), new Color(45/255f, 80/255f, 45/255f, 1), null);
		buttons[3] = new Button(36 / Main.camera.zoom, Gdx.graphics.getHeight() / 2 + 20 / Main.camera.zoom, Button.RECT, Button.ONE_CLICK, 65, 20, new Color(90/255f, 160/255f, 90/255f, 1), new Color(45/255f, 80/255f, 45/255f, 1), null);
		buttons[1] = new Button(36 / Main.camera.zoom, Gdx.graphics.getHeight() / 2 - 20 / Main.camera.zoom, Button.RECT, Button.ONE_CLICK, 65, 20, new Color(90/255f, 160/255f, 90/255f, 1), new Color(45/255f, 80/255f, 45/255f, 1), null);
		buttons[5] = new Button(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - 20 / Main.camera.zoom, Button.RECT, Button.ONE_CLICK, 65, 20, new Color(90/255f, 160/255f, 90/255f, 1), new Color(45/255f, 80/255f, 45/255f, 1), null);

	}
	//Part of the draw loop, checks if any of the buttons are pressed
	public void update() {
		if (!saveScreen) {
			Main.camera.unproject(text[0].set(buttons[0].x + (buttons[0].width / Main.camera.zoom) / 6f, buttons[0].y - buttons[0].height / Main.camera.zoom / 1.2f, 0));
			if (buttons[0].isPressed()) saveScreen = true;
		} else {
			for (int i = 1; i < buttons.length; i++) {
				Main.camera.unproject(text[i].set(buttons[i].x + (buttons[i].width / Main.camera.zoom) / 6f, buttons[i].y - buttons[i].height / Main.camera.zoom / 1.2f, 0));
				//If a save has been selected
				if (buttons[i].isPressed()) {
					save = (byte) i;
					//Moves to the transition screen to prepare the player for the next mission
					Main.state.transition.dispose();
					Main.state.transition = new Transition();
					loadGame();
					Main.state.gamestate = Main.state.transition;
				}
			}
		}
	}
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		update();
		Gdx.gl.glClearColor(0.6f, 0.6f, 0.6f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.setProjectionMatrix(Main.camera.combined);
		renderer.begin(ShapeType.Filled);
		if (!saveScreen) {
			buttons[0].render(renderer);
			renderer.end();
			batch.begin();
		}
		else {
			for (int i = 1; i < buttons.length; i++) {
				buttons[i].render(renderer);
			}
			renderer.end();
			batch.begin();
			for (int i = 1; i < buttons.length; i++) {
				buttons[i].draw(batch);
			}
		}
		if (!saveScreen) Main.font.draw(batch, names[0], text[0].x, text[0].y);
		else {
			for (int i = 1; i < buttons.length; i++) {
				Main.font.draw(batch, names[i], text[i].x, text[i].y);
			}
		}
		batch.end();
	}
	/**Will try to load the save from a file. If it exists, it will continue at the level where the player left off
	 * with the same mission. If it does not exist, the level will be set to 1 and a new mission will be created
	 */
	public void loadGame() {
		File file = new File(names[save] + ".txt");
		if (file.exists()) {
			try {
				List<String> info = new ArrayList<>();
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line;
				while ((line = in.readLine()) != null) {
					info.add(line);
				}
				System.out.println("This is level " + info.get(0) + ", and the mission is " + info.get(info.size() - 1));
				Main.state.transition.setNextLevel(info);
				in.close();
			} catch (IOException e) { }
		} else {
			Main.state.transition.setNextLevel(1);
		}
	}
	/**Saves the game to a file, specifically, the level of the game and the mission details.
	 * The maze and location of enemies/items is not saved, and is recreated when the save is loaded
	 */
	public void saveGame() {
		try {
			File file = new File(names[save] + ".txt");
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(Main.state.transition.getCurrentLevel() + "\n");
			//Converts mission into a list of strings to be written to the file
			List<String> saveMission = Main.state.game.mission.getSaveableMission();
			for (int i = 0; i < saveMission.size(); i++) {
				out.write(saveMission.get(i) + "\n");
			}
			out.close();
		} catch (IOException e) { }
	}
	//Nothing to dispose
	@Override
	public void dispose() {
	}
}