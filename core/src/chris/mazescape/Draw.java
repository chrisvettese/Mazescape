package chris.mazescape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import chris.mazescape.gameobjects.GameObject;
import chris.mazescape.gameobjects.Player;

import java.util.ArrayList;
import java.util.List;

public class Draw {
	/**Location of the timer in game coordinates, if one exists*/
	private Vector3 timeLocation;
	/**Location of the timer in screen coordinates*/
	public float[] timeCoordinates;
	private Color healthBar, currentHealth;
	/**How far away objects can be in x and y distance to be visible to the player*/
	private final float[] onScreenValues = {Main.info.width * Main.camera.zoom / 1.7f, Main.info.height * Main.camera.zoom / 1.6f};
	
	public Draw() {
		timeLocation = new Vector3();
		timeCoordinates = new float[] {Main.info.width - 24 / Main.camera.zoom, 5 / Main.camera.zoom};
		healthBar = new Color(0.18f, 0.18f, 0.18f, 0.4f);
		currentHealth = new Color();
	}
	/**
	 * Renders the game
	 * @param batch
	 * @param renderer
	 * @param px
	 * @param py
	 * @param playerIndex
	 */
	public void draw(SpriteBatch batch, ShapeRenderer renderer, float px, float py, int playerIndex) {
		Main.camera.unproject(timeLocation.set(timeCoordinates[Generation.X], timeCoordinates[Generation.Y], 0));
		drawMaze(renderer, px, py, playerIndex);
		drawObjects(batch, px, py, playerIndex, renderer);
	}
	/**
	 * Maze drawing code
	 * @param renderer
	 * @param px
	 * @param py
	 * @param playerIndex
	 */
	private void drawMaze(ShapeRenderer renderer, float px, float py, int playerIndex) {
		Game game = Main.state.game;
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.setProjectionMatrix(Main.camera.combined);
		//Solid squares, not outlines
		renderer.begin(ShapeType.Filled);
		//Only draws area on the screen
		for (int x = (int) px - 7; x < (int) px + 8; x++) {
			for (int y = (int) py - 5; y < (int) py + 6; y++) {
				if (x >= 0 && y >= 0 && x < game.maze[Generation.X].length && y < game.maze.length) {
					float alpha = 1;
					//If the level uses flashlight
					if (Main.state.game.mission.isDark) {
						alpha = getAlpha(x, px, y, py, playerIndex);
					}
					if (game.maze[y][x]) {
						game.mazeColour.a = alpha;
						renderer.setColor(game.mazeColour);
					} else {
						game.background.a = alpha;
						renderer.setColor(game.background);
					}

					renderer.rect(x * Game.size, y * Game.size, Game.size, Game.size);
				}
			}
		}
		renderer.end();
	}
	/**
	 * Renders objects like the player and enemies
	 * @param batch
	 * @param px
	 * @param py
	 * @param playerIndex
	 * @param renderer
	 */
	private void drawObjects(SpriteBatch batch, float px, float py, int playerIndex, ShapeRenderer renderer) {
		//Objects that have health bars (enemies)
		List<Integer> healthObjects = new ArrayList<>();
		List<GameObject> healthTypes = new ArrayList<>();
		//Objects that also use ShapeRenderer
		List<Integer> renderObjects = new ArrayList<>();
		
		Generation generation = Main.state.game.generation;
		batch.begin();
		float[] playerCoords = {px * Game.size, py * Game.size};
		for (int i = 0; i < generation.types.size; i++) {
			GameObject o = Game.object.objects.get(generation.types.get(i));
			//If object is visible to the player
			boolean onScreen = Math.abs(generation.coordinates.get(i)[Generation.X] - playerCoords[Generation.X]) < onScreenValues[Generation.X] && Math.abs(generation.coordinates.get(i)[Generation.Y] - playerCoords[Generation.Y]) < onScreenValues[Generation.Y];
			if (Game.object.moves.contains(generation.types.get(i))) {
				if (Game.object.enemies.contains(generation.types.get(i)) && onScreen) {
					healthObjects.add(i);
					healthTypes.add(o);
				}
				Main.state.game.control.move(generation.uIndexList.get(i));
			}
			if (onScreen) {
				o.draw(i, batch, px, py, playerIndex);
				//These objects require rendering from ShapeRenderer (for the explosions)
				if (o == GameObject.freeze || o == GameObject.explosive) {
					renderObjects.add(i);
				}
			}
			if (generation.health.get(i) <= 0 && generation.health.get(i) > -1000) Main.state.game.delete(generation.uIndexList.get(i));
		}
		batch.end();
		healthTypes.add(GameObject.player);
		healthObjects.add(playerIndex);
		drawHealth(renderer, healthObjects, healthTypes);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.begin(ShapeType.Filled);
		for (int i = 0; i < renderObjects.size(); i++) {
			Game.object.objects.get(generation.types.get(renderObjects.get(i))).renderDraw(renderObjects.get(i), renderer);
		}
		for (int i = 0; i < Main.state.game.buttons.length; i++) {
			if (i != Game.ITEMS) Main.state.game.buttons[i].render(renderer);
		}
		if (Main.state.game.buttons[Game.MOVE].pressButton) {
			renderer.setColor(Main.state.game.buttons[Game.MOVE].normal);
			renderer.circle(((Player) GameObject.player).playerInfo.x, ((Player) GameObject.player).playerInfo.y, 5, 25);
		}
		renderer.end();
		batch.begin();
		if (Main.state.game.mission.timer == Mission.TIMER_WIN || Main.state.game.mission.timer == Mission.TIMER_LOSE) {
			Main.font.draw(batch, ((Integer) (int) Main.state.game.mission.time).toString(), timeLocation.x, timeLocation.y);
		}
		for (int i = 0; i < Main.state.game.buttons.length; i++) {
			if (i != Game.ITEMS) Main.state.game.buttons[i].draw(batch);
		}
		batch.end();
		//Show item screen
		Main.state.game.item.draw(batch, renderer, Main.state.game.buttons[Game.ITEMS].buttonState);
	}
	/**
	 * Renders the health bars
	 * @param renderer
	 * @param healthObjects
	 * @param healthTypes
	 */
	private void drawHealth(ShapeRenderer renderer, List<Integer> healthObjects, List<GameObject> healthTypes) {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Generation gen = Main.state.game.generation;
		renderer.begin(ShapeType.Filled);
		//Renders the health bars above enemies
		for (int i = 0; i < healthObjects.size() - 1; i++) {
			renderer.setColor(healthBar);
			renderer.rect(gen.coordinates.get(healthObjects.get(i))[Generation.X], gen.coordinates.get(healthObjects.get(i))[Generation.Y] + healthTypes.get(i).texture[0].getHeight(), healthTypes.get(i).texture[0].getWidth(), 4);
			float health = gen.health.get(healthObjects.get(i)) / (float) healthTypes.get(i).health;
			//Calculates health bar colour
			float r = 1 - health;
			renderer.setColor(currentHealth.set(r, health, 0, 1));
			renderer.rect(gen.coordinates.get(healthObjects.get(i))[Generation.X] + 1, gen.coordinates.get(healthObjects.get(i))[Generation.Y] + healthTypes.get(i).texture[0].getHeight() + 1, health * (healthTypes.get(i).texture[0].getWidth() - 2), 2);
		}
		float health = gen.health.get(healthObjects.get(healthObjects.size() - 1)) / (float) healthTypes.get(healthTypes.size() - 1).health;
		float r = 1 - health;
		renderer.setColor(healthBar);
		renderer.rect(timeLocation.x + 8, timeLocation.y - 107, 7, 70);
		renderer.setColor(currentHealth.set(r, health, 0, 1));
		renderer.rect(timeLocation.x + 9, timeLocation.y - 106, 5, health * 68);
		renderer.end();
	}
	/**
	 * For levels that use the flashlight, calculates how bright everything should be
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param playerIndex
	 * @return
	 */
	public float getAlpha(float x1, float x2, float y1, float y2, int playerIndex) {
		Generation generation = Main.state.game.generation;
		x1 /= 2f;
		x2 /= 2f;
		y1 /= 2f;
		y2 /= 2f;
		float alpha = 1 - (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)) / 3.5f;
		if (alpha < 0) alpha = 0;
		if (generation.animation.get(playerIndex)[Controls.ROTATION] == 0) {
			if (x2 > x1 || Math.abs(x1 - x2) < Math.abs(y1 - y2)) alpha = alpha / 10 - 0.05f;
		} else if (generation.animation.get(playerIndex)[Controls.ROTATION] == 90) {
            if (y2 > y1 || Math.abs(y1 - y2) < Math.abs(x1 - x2)) alpha = alpha / 10 - 0.05f;
		} else if (generation.animation.get(playerIndex)[Controls.ROTATION] == 180) {
            if (x2 < x1 || Math.abs(x1 - x2) < Math.abs(y1 - y2)) alpha = alpha / 10 - 0.05f;
		} else {
            if (y2 < y1 || Math.abs(y1 - y2) < Math.abs(x1 - x2)) alpha = alpha / 10 - 0.05f;
		}
		if (alpha < 0) alpha = 0;
		return alpha;
	}
}