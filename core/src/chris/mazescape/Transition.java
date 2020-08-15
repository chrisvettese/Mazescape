package chris.mazescape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import java.util.List;

/**The screen between missions*/
public class Transition extends GameState {
	private int level;
	private List<String> info;
	private Button nextLevel, menu;
	public boolean levelLost;
	
	public Transition() {
		levelLost = false;
		nextLevel = new Button(Gdx.graphics.getWidth() - 80 / Main.camera.zoom, Gdx.graphics.getHeight() - 40 / Main.camera.zoom, Button.RECT, Button.ONE_CLICK, 45, 10, new Color(0.7f, 0.6f, 0.5f, 1), new Color(0.4f, 0.3f, 0.2f, 1), null);
		menu = new Button(80 / Main.camera.zoom, Gdx.graphics.getHeight() - 40 / Main.camera.zoom, Button.RECT, Button.ONE_CLICK, 45, 10, nextLevel.normal, nextLevel.pressed, null);	
	}
	public void setNextLevel(int level) {
		this.level = level;
		this.info = null;
	}
	public void setNextLevel(List<String> info) {
		this.info = info;
	}
	public int getCurrentLevel() {
		return level;
	}
	@Override
	public void draw(SpriteBatch batch, ShapeRenderer renderer) {
		//If no mission is set, create new one based on available information
		if (Main.state.game.mission == null) {
			if (!levelLost) {
				if (info == null) Main.state.game.setLevel(level);
				else Main.state.game.setLevel(info);
			} else {
				Main.state.menu.loadGame();
				Main.state.game.setLevel(info);
			}
		}
		//If player wants to return to menu
		if (menu.isPressed()) {
			levelLost = false;
			Main.state.menu.saveGame();
			Main.state.menu.dispose();
			Main.state.menu = new Menu();
			Main.state.game.dispose();
			Main.state.game = new Game();
			Main.state.gamestate = Main.state.menu;
			return;
		}
		//Starts the game
		else if (nextLevel.isPressed()) {
			levelLost = false;
			Main.state.gamestate = Main.state.game;
			return;
		}
		renderer.begin(ShapeType.Filled);
		nextLevel.render(renderer);
		menu.render(renderer);
		renderer.end();
		batch.begin();
		Main.font.getData().setScale(0.6f, 0.6f);
		//Mission success
		if (level != 1 && !levelLost) {
			Main.font.draw(batch, "Level " + (level - 1) + " Complete!", menu.buttonLocation.x, menu.buttonLocation.y + 40);
		}
		//Level lost screen
		else if (levelLost) {
			Main.font.draw(batch, "Mission Failed", menu.buttonLocation.x, menu.buttonLocation.y + 40);
			Main.font.draw(batch, "Repeat Mission?: " + Main.state.game.mission.description, menu.buttonLocation.x, menu.buttonLocation.y + 30);
			Main.font.draw(batch, "No", menu.buttonLocation.x + 5, menu.buttonLocation.y + 9);
			Main.font.draw(batch, "Yes", nextLevel.buttonLocation.x + 5, nextLevel.buttonLocation.y + 9);
		}
		//General mission screen
		if (!levelLost) {
			Main.font.draw(batch, "New Mission: " + Main.state.game.mission.description, menu.buttonLocation.x, menu.buttonLocation.y + 30);
			Main.font.draw(batch, "Menu", menu.buttonLocation.x + 5, menu.buttonLocation.y + 9);
			Main.font.draw(batch, "Start Level", nextLevel.buttonLocation.x + 1, nextLevel.buttonLocation.y + 9);
		}
		Main.font.getData().setScale(1, 1);
		batch.end();
	}
	//Nothing to dispose
	@Override
	public void dispose() {
	}
}