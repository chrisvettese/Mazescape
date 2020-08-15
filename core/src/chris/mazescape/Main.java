package chris.mazescape;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**This is the starting class for the game. It contains the create, render, and dispose methods*/
public class Main extends ApplicationAdapter {
	/**Used for drawing images*/
	private SpriteBatch batch;
	/**Used for drawing shapes like rectangles*/
	private ShapeRenderer renderer;
	
	/**Class to manage different parts of the game*/
	public static GameState state;
	/**Used for zooming differently on each phone, and following the player. More efficient than doing it manually*/
	public static OrthographicCamera camera;
	/**Font for all the text in the game*/
	public static BitmapFont font;
	public static GameInfo info;
	
	/**
	 * This is called when the game starts. Similar to processing's setup method
	 */
	@Override
	public void create () {
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		info = new GameInfo();
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();
		renderer.setAutoShapeType(true);
		camera.zoom = info.zoom;
		camera.update();
		font = new BitmapFont();
		//Stops font from rounding, since it is noticeable when zoomed in as much as this game
		font.setUseIntegerPositions(false);
		font.setColor(1, 1, 1, 1);
		//Makes font look nicer
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Nearest);
		state = new GameState();
		state.load();
	}
	/**
	 * This is the looping part of the game. Similar to processing's draw method
	 */
	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		state.draw(batch, renderer);
	}
	/**
	 * When the game is over, all textures and other resources must be "disposed" so to not cause a memory leak
	 * In larger games this can make the game a lot more efficient since textures can be disposed of when they
	 * are not needed at the exact moment
	 */
	@Override
	public void dispose () {
		batch.dispose();
		renderer.dispose();
		state.dispose();
		font.dispose();
	}
}
